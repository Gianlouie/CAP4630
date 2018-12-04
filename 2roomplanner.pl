
%%%%%%%%% Simple Prolog Planner %%%%%%%%%%%%%%%%%%%%%%%%%%
%%%
%%% Based on one of the sample programs in:
%%%
%%% Artificial Intelligence:
%%% Structures and strategies for complex problem solving
%%%
%%% by George F. Luger and William A. Stubblefield
%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

/*  UCF
	Gianlouie Molinary gi713278
	CAP 4630 Fall 2018
*/

:- module( planner,
	   [
	       plan/4,change_state/3,conditions_met/2,member_state/2,
	       move/3,go/2,test1/0,test2/0,test4/0
	   ]).

:- [utils].

plan(State, Goal, _, Moves) :-	equal_set(State, Goal),
				write('moves are'), nl,
				reverse_print_stack(Moves).
plan(State, Goal, Been_list, Moves) :-
				not(member_set(inroom(hand, 1),State)),
				not(member_set(inroom(hand, 2), State)),
				move(Name, Preconditions, Actions),
				conditions_met(Preconditions, State),
				change_state(State, Actions, Child_state),
				not(member_state(Child_state, Been_list)),
				stack(Child_state, Been_list, New_been_list),
				stack(Name, Moves, New_moves),
			plan(Child_state, Goal, New_been_list, New_moves),!.
plan(State, Goal, Been_list, Moves) :-
				move2(Name, Preconditions, Actions),
				conditions_met(Preconditions, State),
				change_state(State, Actions, Child_state),
				not(member_state(Child_state, Been_list)),
				stack(Child_state, Been_list, New_been_list),
				stack(Name, Moves, New_moves),
			plan(Child_state, Goal, New_been_list, New_moves),!.
			

change_state(S, [], S).
change_state(S, [add(P)|T], S_new) :-	change_state(S, T, S2),
					add_to_set(P, S2, S_new), !.
change_state(S, [del(P)|T], S_new) :-	change_state(S, T, S2),
					remove_from_set(P, S2, S_new), !.
conditions_met(P, S) :- subset(P, S).

member_state(S, [H|_]) :-	equal_set(S, H).
member_state(S, [_|T]) :-	member_state(S, T).

/* move types */

move(pickup(X), [handempty, clear(X), on(X, Y)],
		[del(handempty), del(clear(X)), del(on(X, Y)),
				 add(clear(Y)),	add(holding(X))]).

move(pickup(X), [handempty, clear(X), ontable(X)],
		[del(handempty), del(clear(X)), del(ontable(X)),
				 add(holding(X))]).

move(putdown(X), [holding(X)],
		[del(holding(X)), add(ontable(X)), add(clear(X)),
				  add(handempty)]).

move(stack(X, Y), [holding(X), clear(Y)],
		[del(holding(X)), del(clear(Y)), add(handempty), add(on(X, Y)),
				  add(clear(X))]).
				  
move2(pickup(X), [handempty, clear(X), on(X, Y), inroom(X,Z), inroom(hand,Z)],
		[del(handempty), del(clear(X)), del(on(X, Y)),
				 add(clear(Y)),	add(holding(X))]).
				 
move2(pickup(X), [handempty, clear(X), ontable(X), inroom(X,Z), inroom(hand,Z)],
		[del(handempty), del(clear(X)), del(ontable(X)),
				 add(holding(X))]).

move2(stack(X, Y), [holding(X), clear(Y), inroom(Y,Z), inroom(hand,Z)],
		[del(holding(X)), del(clear(Y)), add(handempty), add(on(X, Y)),
				  add(clear(X))]).

move2(putdown(X), [holding(X)],
		[del(holding(X)), add(ontable(X)), add(clear(X)),
				  add(handempty)]).

move2(goroom2, [holding(X), inroom(hand,1)],
		[del(inroom(X,1)), add(inroom(X,2)), del(inroom(hand,1)), add(inroom(hand,2))]).

move2(goroom1, [holding(X), inroom(hand,2)],
		[del(inroom(X,2)), add(inroom(X,1)), del(inroom(hand,2)), add(inroom(hand,1))]).

move2(goroom2, [handempty, inroom(hand,1)],
		[del(inroom(hand,1)), add(inroom(hand,2))]).

move2(goroom1, [handempty, inroom(hand,2)],
		[del(inroom(hand,2)), add(inroom(hand,1))]).

inroom(X,Y) :- inroom(Z,Y), on(X,Z).

go(S, G) :- plan(S, G, [S], []).

test4 :- go([handempty, inroom(hand,1), ontable(b), inroom(b,1), inroom(a,1), on(a, b), clear(a)],
	          [handempty, inroom(hand,1), ontable(b), inroom(b,2), inroom(a,2),  on(a, b), clear(a)]).

test1 :- go([handempty, inroom(hand,1), ontable(b), ontable(c), inroom(b,1), inroom(c,1), inroom(a,1), on(a,b), clear(a), clear(c)],
				[handempty, inroom(hand,1), ontable(c), inroom(a,1), inroom(b,1), inroom(c,1), on(b,c), on(a,b), clear(a)]).

test2 :- go([handempty, inroom(hand,1), ontable(b), ontable(c), inroom(b,1), inroom(c,1), inroom(a,1), on(a,b), clear(a), clear(c)],
				[handempty, inroom(hand,1), inroom(a,2), inroom(b,2), inroom(c,2), ontable(b), on(c,b), on(a,c), clear(a)]).