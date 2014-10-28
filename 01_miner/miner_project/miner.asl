!start.

+!start : name(N) & N = miner1 <- +boss;!doIt.
+!start : name(N) & N = miner2 <- +boss;!doIt.
+!start : true <- !doIt.

// closest unpicked gold
nextGold(X,Y) :- freeGold(X,Y) & not(freeGold(W,_) & W > X) & not(freeGold(X,Z) & Z > Y).
// gold in database which is not already picked
freeGold(X,Y) :- g(X,Y) & not pickedGold(X,Y).

// standing on not registered deposit? Register it!
+!doIt : pos(X,Y) & cell(X,Y,depot) & not(deposit(X,Y))  <-
/*	.broadcast(tell, deposit(X,Y));
	.print("Deposit?");*/
	.send(miner1, tell, deposit(X,Y));
	.send(miner2, tell, deposit(X,Y));
	.send(miner3, tell, deposit(X,Y));
	.send(miner4, tell, deposit(X,Y));
	!doIt.
	
// standing on not registered gold? Register it!
+!doIt : pos(X,Y) & cell(X,Y,gold) & not(g(X,Y)) <-
	//.broadcast(tell, g(X,Y));/*
	.send(miner1, tell, g(X,Y));
	.send(miner2, tell, g(X,Y));
	.send(miner3, tell, g(X,Y));
	.send(miner4, tell, g(X,Y));
	.print("found new gold ", X, ",", Y);
	!doIt.
	
// standing on unregistered cell? register it!
+!doIt : pos(X,Y) & not(visited(X,Y)) <-
	//.broadcast(tell, visited(X,Y));/**/
	.send(miner1, tell, visited(X,Y));
	.send(miner2, tell, visited(X,Y));
	.send(miner3, tell, visited(X,Y));
	.send(miner4, tell, visited(X,Y));
	!doIt.

// if we have gold and are standing on the depot, drop it and get back to work	
+!doIt : carrying_gold & pos(X,Y) & cell(X,Y,depot) <- do(drop);.print("saved gold and was proud of it."); !doIt.	

// boss 1 wants his slave to help him pick up the gold
+!doIt : name(N) & N = miner1 & pos(X,Y) & cell(X,Y, gold) & not carrying_gold 
	<- .send(miner3, tell, helpMe(X,Y)); !pickGold.

// boss 2 wants his slave to help him pick up the gold
+!doIt : name(N) & N = miner2 & pos(X,Y) & cell(X,Y, gold) & not carrying_gold 
	<-.send(miner4, tell, helpMe(X,Y)); !pickGold.

// if we are boss and are not carying gold, go fetch closest gold from database, let us allow small deviance
+!doIt : boss & not(carrying_gold) & pos(_,Y) & nextGold(_,Z) & Z < Y <- do(up); !doIt.
+!doIt : boss & not(carrying_gold) & pos(_,Y) & nextGold(_,Z) & Z > Y <- do(down); !doIt.
+!doIt : boss & not(carrying_gold) & pos(X,_) & nextGold(W,_) & W < X <- do(left); !doIt.
+!doIt : boss & not(carrying_gold) & pos(X,_) & nextGold(W,_) & W > X <- do(right); !doIt.
	
//if we are carying a gold, go save it, miner. Go!
+!doIt : carrying_gold & pos(_,Y) & deposit(_,Z) & Z < Y <- do(up); !doIt.
+!doIt : carrying_gold & pos(_,Y) & deposit(_,Z) & Z > Y <- do(down); !doIt.
+!doIt : carrying_gold & pos(X,_) & deposit(W,_) & W < X <- do(left); !doIt.
+!doIt : carrying_gold & pos(X,_) & deposit(W,_) & W > X <- do(right); !doIt.

// helpMe can know only slave, so, we are slave and have to go help our boss!
+!doIt : helpMe(_,Z) & pos(_,Y) & Z < Y <- do(up); !doIt.
+!doIt : helpMe(_,Z) & pos(_,Y) & Z > Y <- do(down); !doIt.
+!doIt : helpMe(W,_) & pos(X,_) & W < X <- do(left); !doIt.
+!doIt : helpMe(W,_) & pos(X,_) & W > X <- do(right); !doIt.

// otherwise, we are off duty, so let us explore the unknown areas
+!doIt : pos(X,Y) & not(visited(X,Y-1)) & Y > 0 <- do(up); !doIt.
+!doIt : pos(X,Y) & not(visited(X-1,Y)) & X > 0 <- do(left); !doIt.
+!doIt : pos(X,Y) & not(visited(X+1,Y)) & gsize(_,S,_) & X + 1 < S <- do(right); !doIt.
+!doIt : pos(X,Y) & not(visited(X,Y+1)) & gsize(_,_,S) & Y + 1 < S <- do(down); !doIt.

// if we are in middle of known area, do some random movement just to keep us busy
+!doIt : math.random <= 0.25 <- do(left); !doIt.
+!doIt : math.random <= 0.333333 <- do(right); !doIt.
+!doIt : math.random <= 0.5 <- do(up); !doIt.
+!doIt : true <- do(down);!doIt.

// boss wants to pick up gold, but he can do it only if in his 4-neighbourhood is standing another agent
// otherwise he will do nothing
+!pickGold : name(N) & N = miner1 & pos(X,Y) & 
			(cell(X,Y+1,ally) | cell(X,Y-1,ally) | cell(X+1,Y,ally) | cell(X-1,Y,ally)) <- 
			
	do(pick);
// tell the other boss and yourself that this gold is picked
	.send(miner1, tell, pickedGold(X,Y));
	.send(miner2, tell, pickedGold(X,Y));
// release the slave	
	.send(miner3, untell, helpMe(X,Y));
	!doIt.
	
+!pickGold : name(N) & N = miner2 & pos(X,Y) & 
			(cell(X,Y+1,ally) | cell(X,Y-1,ally) | cell(X+1,Y,ally) | cell(X-1,Y,ally)) <- 
			
	do(pick);
// tell the other boss and yourself that this gold is picked
	.send(miner1, tell, pickedGold(X,Y));
	.send(miner2, tell, pickedGold(X,Y));
// release the slave	
	.send(miner4, untell, helpMe(X,Y));
	!doIt.
	
+!pickGold : true <- do(skip);!pickGold.
-!pickGold : true <- do(skip);!pickGold.
