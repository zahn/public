:- module(plparser,[
	parse/1,
	parse/2	
]).

:- dynamic node_attr/2.
:- dynamic node_id/1.


  

parse(File,InStream):-
    writeln(ta),
    unused_id(Id),
    my_assert(node_attr(Id,type(compilation_unit))),
    my_assert(node_attr(Id,file(File))),
    source_folder(FolderId,File),
    my_assert(node_attr(Id,parent(FolderId))),
%    catch(
	!,
    	parse_clauses(Id,File,InStream,user).
%    	eof,
%    	(!,true)
%    ).

parse(File):-
    writeln(tu),
    open(File,read,InStream),
    parse(File,InStream),
    close(InStream).

source_folder(FolderId,File):-
    file_directory_name(File,Dir),
    (	(	node_attr(FolderId,file(Dir)),
			node_attr(FolderId,type(source_folder))
		)
	;	unused_id(FolderId),
		my_assert(node_attr(FolderId,file(Dir))),
		my_assert(node_attr(FolderId,type(source_folder)))
	).

parse_clauses(FileId,File,InStream,Module):-
    writeln(to),
    unused_id(Id),
    my_assert(node_attr(Id,parent(FileId))),
    !,
	parse_clause(Id,File,Module,InStream,NextModule),
	!,
	parse_clauses(FileId,File,InStream,NextModule).

    

parse_clause(Id,File,Module,InStream,NextModule):-
    writeln(tem),
	catch(
		read_term(InStream,Term,[term_position(Pos),subterm_positions(Sub),module(Module)]),
		error(Error,Context),
		(handle_error(Id,Error,Context,File),fail)
	),	
    format("processing ~w~n",[Term]),
	arg(2,Pos,Line)	,
    writeln(te1),
	!,
	\+ Term==end_of_file,
    writeln(te2),
	next_module(Term,NextModule),	    
	parse_subterm_positions(Id,Term,Sub),
   	my_assert(node_attr(Id,file(File))),
   	my_assert(node_attr(Id,line(Line))),      
   	(	find_clauseref(File,Line,Term,ClauseRef)
   	->	my_assert(node_attr(Id,clause(ClauseRef)))
   	;	my_assert(node_attr(Id,no_clauseref_found))
   	).



next_module(Term,NextModule):-
    writeln(li),
    Term=..[:-,Directive],
    Directive=..[module,NextModule,_],
    !.
next_module(_,user):-
	    writeln(lo).    
% find the smallest clause reference that 
% matches the term, aswell as the file and the line
% and is not already associated to some node.
%
% Warning:
% We assume that the order in which identical clauses are encountered
% in a file correpsonds to the order of their references. This is
% a rather strong assumption, which i would love to get rid of. Any suggestions?    
find_clauseref(File,Line,Term,ClauseRef):-
    writeln(lum),
    Term=..[Functor|Args],   
    setof(Canidate,find_clauseref(File,Line,Functor,Args,Canidate),[ClauseRef|_]).

find_clauseref(File,Line,:-,[Head,Body],ClauseRef):-
    writeln(re),
    clause(Head,Body,ClauseRef),
    clause_property(ClauseRef,file(File)),
    clause_property(ClauseRef,line_count(Line)),
    \+ node_attr(_,clause(ClauseRef)).
       
find_clauseref(File,Line,Fact,_,ClauseRef):-
    writeln(ro),    
    clause(Fact,true,ClauseRef),
    clause_property(ClauseRef,file(File)),
    clause_property(ClauseRef,line_count(Line)),
    \+ node_attr(_,clause(ClauseRef)).    

    
handle_error(Id,Error,Context,File):-
	my_assert(node_attr(Id,error(Error))),    
	my_assert(node_attr(Id,context(Context))),
	my_assert(node_attr(Id,file(File))).
	
parse_subterm_positions(Id,Term, From-To):-
	my_assert(node_attr(Id,From-To)),
	( term_variables(Term,[])
	->my_assert(node_attr(Id,type(atom)))
	; my_assert(node_attr(Id,type(variable)))
	),
	my_assert(node_attr(Id,term(Term))).
	
parse_subterm_positions(Id,Term, string_position(From,To)):-
	my_assert(node_attr(Id,From-To)),
	my_assert(node_attr(Id,type(string))),
	my_assert(node_attr(Id,term(Term))).

parse_subterm_positions(Id,Term,  brace_term_position(From, To, Arg)):-
   	my_assert(node_attr(Id,From-To)),
	my_assert(node_attr(Id,type(brace))),
	my_assert(node_attr(Id,term(Term))),
	unused_id(ArgId),
	arg(1,Term,ArgTerm),
	parse_subterm_positions(ArgId,ArgTerm,Arg),
	my_assert(node_attr(Id,term(Term))),
	my_assert(node_attr(Id,argument(ArgId))),
	my_assert(node_attr(ArgId,parent(Id))).

parse_subterm_positions(Id,Term,  list_position(From, To, Elms, Tail)):-
   	my_assert(node_attr(Id,From-To)),
	my_assert(node_attr(Id,type(list))),
	my_assert(node_attr(Id,term(Term))),
	parse_elm_positions(Id,Term,Elms,Tail,ElmIDs,TailId),
	my_assert(node_attr(Id,elements(ElmIDs))),
	my_assert(node_attr(Id,tail(TailId))).
    
parse_subterm_positions(Id,Term,  term_position(From, To, FFrom, FTo, SubPos)):-
    my_assert(node_attr(Id,From-To)),
    my_assert(node_attr(Id,functor_position(FFrom-FTo))),
    functor(Term,Name,Arity),
    my_assert(node_attr(Id,functor(Name/Arity))),
	my_assert(node_attr(Id,type(compound))),
	my_assert(node_attr(Id,term(Term))),
    Term=..[_|Args],
   	parse_arg_positions(Id,Args,SubPos,ArgIDs),
   	my_assert(node_attr(Id,arguments(ArgIDs))).

parse_arg_positions(_,[],[],[]).   	

parse_arg_positions(ParentId,[Arg|Args],[Position|Positions],[Id|Ids]):-
    unused_id(Id),
   	my_assert(node_attr(Id,parent(ParentId))),
   	parse_subterm_positions(Id,Arg,Position),
   	parse_arg_positions(ParentId,Args,Positions,Ids).

parse_elm_positions(_,[],[],_,[],_).   	

parse_elm_positions(ParentId,Elm,[],TailPosition,[],TailId):-
    unused_id(TailId),
   	my_assert(node_attr(TailId,parent(ParentId))),
   	parse_subterm_positions(TailId,Elm,TailPosition).
   	
parse_elm_positions(ParentId,[Elm|Elms],[Position|Positions],TailPosition,[Id|Ids],TailId):-
    unused_id(Id),
   	my_assert(node_attr(Id,parent(ParentId))),
   	parse_subterm_positions(Id,Elm,Position),
   	parse_elm_positions(ParentId,Elms,Positions,TailPosition,Ids,TailId).

my_assert(node_id(Id)):-
    %format("assert(~w)~n",[Term]),
    ( 	node_id(Id)
	;   assert(node_id(Id))
	).

my_assert(node_attr(Id,Attr)):-
    %format("assert(~w)~n",[Term]),
    my_assert(node_id(Id)),
    (	node_attr(Id,Attr)
    ;	assert(node_attr(Id,Attr))
    ).
    
unused_id(source_node(Num)):-
    Try is random(1073741824),
    ( node_id(source_node(Try))
    ->unused_id(source_node(Num))
    ; Num=Try
    ).