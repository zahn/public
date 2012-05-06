
:- module( pdt_editor_reload,
         [ pdt_reload/1                           % Called from ConsultActionDelegate.run()
         , activate_warning_and_error_tracing/0   % Called from PLMarkerUtils.addMarkers()
         , deactivate_warning_and_error_tracing/0 % Called from PLMarkerUtils.addMarkers()
         , errors_and_warnings/5                  % Called from PLMarkerUtils.run()
         , pdt_reloaded_file/1
         ]).


               /*************************************
                * PDT RELAOAD                       *
                *************************************/

:- use_module('../split_file_path.pl').
:- use_module(library(make)).

:- op(600, xfy, ::).   % Logtalk message sending operator

%% pdt_reload(+File) is det.
%
% Wrapper for consult used to ignore PLEditor-triggered consults in the
% console history and to start the update of the PDT-internal factbase.

% SWI-Prolog list    
pdt_reload(Files):-
    is_list(Files),
    !,
    forall(member(F,Files),pdt_reload_no_listener(F)),
	notify_reload_listeners(Files).
	
% Logtalk
pdt_reload(File):-
    split_file_path(File, _Directory,_FileName,_,lgt),
    !,
    with_mutex('reloadMutex', 
      logtalk_adapter::pdt_reload(File)
    ).

% SWI-Prolog
pdt_reload(File):-
    debug(pdt_reload, 'pdt_reload(~w)', [File]),
    with_mutex('reloadMutex', 
      (
        % we have to continiue, even if reload_file fails
        % normally failing means: the file has errors
        (make:reload_file(File) -> true ; true),
        % only return false if the file is not loaded at all
        ( source_file(File) ->
          ( retractall(in_reload),
            notify_reload_listeners([File])
          )
          ; fail
        )        
      )
    ).
    
pdt_reload_no_listener(File):-
    debug(pdt_reload, 'pdt_reload_no_listener(~w)', [File]),
    with_mutex('reloadMutex', 
      (
        % we have to continiue, even if reload_file fails
        % normally failing means: the file has errors
        (make:reload_file(File) -> true ; true),
        % only return false if the file is not loaded at all
        ( source_file(File) ->
          retractall(in_reload)
          ; fail
        )        
      )
    ).

:- multifile(pdt_reload_listener/1).

notify_reload_listeners(Files) :-
	pdt_reload_listener(Files),
	fail.
notify_reload_listeners(_).

pdt_reload_listener(Files) :-
    list_2_separated_list(Files, '<>', FileList),
    catch(pif_observe:pif_notify(file_loaded,FileList),_,true).

               /*************************************
                * INTERCEPT PROLOG ERROR MESSAGES   *
                *************************************/

% Store SWI-Prolog error and warning messages as
% traced_messages(Level, Line, Lines, File) facts.

:- dynamic(traced_messages/4).
:- dynamic(warning_and_error_tracing/0).
:- dynamic(reloaded_file/1).

activate_warning_and_error_tracing :- 
	with_mutex('reloadMutex', (
		begin_reload,
	 	assertz(warning_and_error_tracing)
	)).

deactivate_warning_and_error_tracing :-
	with_mutex('reloadMutex', (
	  retractall(traced_messages(_,_,_,_)),
	  retractall(reloaded_file(_)),
	  retractall(warning_and_error_tracing)
	)). 
 
 	
:- dynamic in_reload/0.

begin_reload :-
    writeln('INFO: begin reload'),
    with_mutex('reloadMutex', assert(in_reload) ),
    trace_reload(begin).
    
%% message_hook(+Term, +Level,+ Lines) is det. 
%
% intercept prolog messages to collect term positions and 
% error/warning messages in traced_messages/3
% 
% @author trho
%  

:- multifile(user:message_hook/3).
:- dynamic(user:message_hook/3).

user:message_hook(_Term, Level,Lines) :-
    with_mutex('reloadMutex', (
		warning_and_error_tracing,
		prolog_load_context(term_position, '$stream_position'(_,Line,_,_,_)),
		prolog_load_context(source, File),
		assertz(traced_messages(Level, Line,Lines, File)),
		trace_reload(traced_messages(Level, Line,Lines, File)),
	%	assertz(user:am(_Term, Level,Lines)),
		fail
	)).


user:message_hook(load_file(start(_, file(_, FullPath))), _, _) :-
	with_mutex('reloadMutex', (
		warning_and_error_tracing,
		assertz(reloaded_file(FullPath)),
		fail
	)).
               /*************************************
                * USE INTERCEPTED PROLOG ERROR MESSAGES   *
                *************************************/

%% errors_and_warnings(?Level,?Line,?Length,?Message,?File) is nondet.
%
errors_and_warnings(Level,Line,0,Message, File) :-
		wait_for_reload_finished,
	    traced_messages(Level, Line, Lines, File),
	    trace_reload(e_w(Lines)),
	%	traced_messages(error(syntax_error(_Message), file(_File, StartLine, Length, _)), Level,Lines),
	    new_memory_file(Handle),
	   	open_memory_file(Handle, write, Stream),
		print_message_lines(Stream,'',Lines),
	    close(Stream),
		memory_file_to_atom(Handle,Message),
	    free_memory_file(Handle).

pdt_reloaded_file(LoadedFile) :-
	wait_for_reload_finished,
	reloaded_file(LoadedFile).
   
wait_for_reload_finished :-
   reset_timout_counter,
   repeat,
   ( with_mutex('reloadMutex', (
      trace_reload(check_in_reload),
       \+in_reload
     ))
    ; ( 
        %writeln(wait_for_reload_to_end),
        trace_reload(wait),
        sleep(0.1),
        ( timeout_reached(Timeout) ->
          throw(reload_timeout_reached(Timeout))
        ; fail 
        )
	  )
	),
    !.


:- dynamic timeout_counter/1.
timeout_threshold(150).

reset_timout_counter :-
   retractall(timeout_counter(_)),
   assert(timeout_counter(0)).
   
   
timeout_reached(New) :-
   timeout_counter(C),
   New is C+1,
   timeout_threshold(Th),
  ( Th == New ->
     true
    ; ( retractall(timeout_counter(_)),
        assert(timeout_counter(New)),
        fail
    )
   ).

% If you want to trace reloading comment out the "fail"
% in the first line of "trace_reload" and then look at
% the reload_trace(What,Time) facts generated. 
% It makes no sense to add a special preference to enable
% reload tracing since this only interests PDT developers, 
% not end users: 
:- dynamic reload_trace/2.

trace_reload(Name):-
    fail,
    get_time(T),
    assert(reload_trace(Name,T)),
    !.
trace_reload(_Name).    