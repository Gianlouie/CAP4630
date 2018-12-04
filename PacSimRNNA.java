/*
 * University of Central Florida
 * CAP 4630 - Fall 2018
 * Author: Gianlouie Molinary
 */

import java.awt.Point;
import java.util.*;
import pacsim.BFSPath;
import pacsim.PacAction;
import pacsim.PacCell;
import pacsim.PacFace;
import pacsim.PacSim;
import pacsim.PacUtils;
import pacsim.PacmanCell;

class PacPotential implements Comparable<PacPotential>
{
    public int size;
    public int cost;
    public ArrayList<Point> path;
    public List<Point> clean;
    private HashMap<Point,Integer> directCost;

    PacPotential(Point position, int c, PacCell[][] grid, int[][] costTable)
    {
        path = new ArrayList<Point>();
        clean = new ArrayList<Point>();
        path.add(position);
        clean = PacUtils.findFood(grid);
        createHash(costTable, clean);
        clean.remove(position);
        cost = c;
        size = 1;
    }

    PacPotential(ArrayList<Point> path, int cost, List<Point> grid, PacCell[][] clone, int[][] costTable)
    {   
        this.path = new ArrayList<Point>();
        clean = grid;
        this.cost = cost;
        size = path.size();
        List<Point> genGrid = new ArrayList<Point>();
        this.path.addAll(path);
        genGrid.addAll(grid);
        createHash(costTable,PacUtils.findFood(clone));
    }

    public void addLocation(Point loc)
    {
        path.add(loc);
        clean.remove(loc);
        size++;
    }

    public Point getCurrentLocation()
    {
        return path.get(size-1);
    }

    public ArrayList<Point> getPath()
    {
        return path;
    }

    public void setCurrentCost(int c)
    {
        cost += c;
    }

    public int getCurrentCost()
    {
        return cost;
    }

    private void createHash(int[][] costTable, List<Point> food)
    {
        directCost = new HashMap<Point,Integer>();

        for(int i = 0 ; i < food.size(); i++)
            directCost.put(food.get(i),costTable[0][i+1]);
    }

    public void PrintPopulation(int size, PacCell[][] grid)
    {
        if(size > path.size() + clean.size())
            size--;

        System.out.print(" :  cost=" + cost + " : " );

        Point p = path.get(0);
        int x = (int)p.getX();
        int y = (int)p.getY();
        int c = directCost.get(p);

        System.out.print("[(" + x + "," + y + "),"+ c +"] ");
        
        for(int i = 1; i < size; i++)
        {
            p = path.get(i);
            x = (int)p.getX();
            y = (int)p.getY();
            c = BFSPath.getPath(grid,path.get(i),path.get(i-1)).size();
            System.out.print("[(" + x + "," + y + "),"+ c +"] ");
        }

        System.out.println();
    }

    public List<Point> getRemainingFood()
    {
        return this.clean;
    }

    @Override
    public int compareTo(PacPotential path)
    {
        int comp =((PacPotential)path).getCurrentCost();
        return this.cost - comp;
    }
}

public class PacSimRNNA implements PacAction 
{
    private static List<Point> path;
    private int simTime;
    private PacmanCell pc;
    private List<Point> allPacPotentials;
    private long timeMilliStart;
    private long timeMilliStop;
    private boolean firstTime = true;
    public static List<Point> targets;
    public static HashSet<Point> dirty;

    public PacSimRNNA(String fname) 
    {
        PacSim sim = new PacSim(fname);
        sim.init(this);
    }

    public static void main(String[] args) 
    {
        System.out.println("\nTSP using Repetitive Nearest Neighbor Algorithm by Gianlouie Molinary");
        System.out.println("\nMaze: " + args[0] + "\n");
        new PacSimRNNA(args[0]);
    }

    @Override
    public void init() 
    {
        simTime = 0;
        path = new ArrayList<Point>();
        allPacPotentials = new ArrayList<Point>();
    }

    private ArrayList<Point> closeFood(Point p, List<Point> foodList, PacCell[][] grid)
    {
        if(foodList.size() <= 0)
            return null;

        ArrayList<Point> closestFood = new ArrayList<Point>();
        Point newLoc = foodList.get(0);
        int cost = BFSPath.getPath(grid,p,newLoc).size();

        for(int i = 1; i < foodList.size(); i++)
        {
            newLoc = foodList.get(i);
            int newCost = BFSPath.getPath(grid,p,newLoc).size();

            if(newCost < cost)
            {
                cost = newCost;
            }
        }

        for(int i = 0; i < foodList.size(); i++)
        {
            newLoc = foodList.get(i);
            int pathCost = BFSPath.getPath(grid,p,newLoc).size();

            if(pathCost == cost)
            {
                closestFood.add(newLoc);
            }
        }
        
        return closestFood;
    }

    public List<Point> planner(PacCell[][] grid, PacmanCell pc)
    {
        System.out.println();

        List<Point> food = PacUtils.findFood(grid);
        int size = PacUtils.numFood(grid);
        Point pacmanLoc = pc.getLoc();
        List<Point> pacPoint;
        List<Point> pacPoint2;
        Point x;
        Point y;
        int xLen;
        int yLen;

        System.out.println("Cost Table:\n");

        int numFood = food.size();
        int [][] costTable = new int[numFood+1][numFood+1];

        for (int i = 0; i < numFood; i++)
        {
            for (int j = 0; j < numFood; j++)
            {
                x = food.get(i);
                y = food.get(j);

                pacPoint = BFSPath.getPath(grid, pacmanLoc, x);
                pacPoint2 = BFSPath.getPath(grid, pacmanLoc, y);

                xLen = pacPoint.size();
                yLen = pacPoint2.size();

                costTable[i+1][0] = xLen;
                costTable[0][j+1] = yLen;
            }
        }

        for (int i = 0; i < numFood; i++)
        {
            for (int j = 0; j < numFood; j++)
            {
                x = food.get(i);
                y = food.get(j);
                pacPoint = BFSPath.getPath(grid, x, y);
                int len = pacPoint.size();

                costTable[i+1][j+1] = len;
            }
        }

        for (int i = 0; i < numFood+1; i++)
        {
            for (int j = 0; j < numFood+1; j++)
                System.out.printf("%3d", costTable[i][j]);

            System.out.println();
        }

        System.out.println("\nFood Array:\n");

        int foodLen = food.size();

        for (int i = 0; i < foodLen; i++)
        {
            int xloc = (int)(food.get(i)).getX();
            int yloc = (int)(food.get(i)).getY();

            System.out.println(i + " : (" + xloc + "," + yloc + ")");
        }

        System.out.println();

        int lowest = 100;
        int cost = 0;

        ArrayList<PacPotential> Table = new ArrayList<PacPotential>(size);

        for (int row = 0; row < size; row++)
        {
            Point position = food.get(row);

            cost = BFSPath.getPath(grid, pacmanLoc, position).size();
            PacPotential n = new PacPotential(position, cost, PacUtils.cloneGrid(grid), costTable);

            Table.add(n);
        }

        cost = 0;
        List<Point> bestPacPath = new ArrayList<Point>();
        int PacPotentialIndex = 0;
        int table = food.size();
        int stepNumber = 0;

        for(int f = 0; f < food.size(); f++)
        {
            System.out.println();
            System.out.println("Population at step " + (stepNumber+1) + " : ");

            stepNumber++;

            System.out.println();

            table = Table.size();
            PacPotentialIndex = 0;
            ArrayList<PacPotential> tempTable = new ArrayList<PacPotential>();

            for (int i = 0; i < Table.size(); i++)
            {
                PacPotential pac = Table.get(i);
                Point loc = pac.getCurrentLocation();
                ArrayList<Point> closestFood = closeFood(loc, pac.getRemainingFood(), grid);
                
                if (closestFood != null)
                {
                    for (int j = 1; j < closestFood.size(); j++)
                    {
                        Point newLoc = closestFood.remove(j);
                        List<Point> newGrid = new ArrayList<Point>();
                        ArrayList<Point> newPath = new ArrayList<Point>();
                        newGrid = PacUtils.clonePointList(pac.getRemainingFood());
                        newPath.addAll(pac.getPath());
                        PacPotential temp = new PacPotential(newPath, pac.getCurrentCost(), newGrid, PacUtils.cloneGrid(grid), costTable);

                        int newCost = BFSPath.getPath(grid, loc, newLoc).size();
                        temp.setCurrentCost(newCost);
                        temp.addLocation(newLoc);
                        tempTable.add(temp);
                    }

                    int newCost = BFSPath.getPath(grid, loc, closestFood.get(0)).size();
                    pac.setCurrentCost(newCost);
                    pac.addLocation(closestFood.get(0));
                }
            }

            Collections.sort(Table);

            System.out.print(PacPotentialIndex++);
            Table.get(0).PrintPopulation(stepNumber, grid);

            for (int i = 1; i < Table.size(); i++)
            {
                System.out.print(PacPotentialIndex++);
                Table.get(i).PrintPopulation(stepNumber, grid);
            }

            Table.addAll(tempTable);
        }

        for (int i = 0; i < Table.size(); i++)
        {
            if (i == 0)
            {
                cost = Table.get(0).getCurrentCost();
                bestPacPath = Table.get(0).getPath();
            }

            else if (Table.get(i).getCurrentCost() < cost)
            {
                cost = Table.get(i).getCurrentCost();
                bestPacPath = Table.get(i).getPath();
            }
        }

        return bestPacPath;
    }

    @Override
    public PacFace action(Object state) 
    {
        PacCell[][] grid = (PacCell[][]) state;
        pc = PacUtils.findPacman(grid);

        if (pc == null) return null;

        if (firstTime)
        {
            timeMilliStart = System.currentTimeMillis();
            targets = planner(grid, pc);
            timeMilliStop = System.currentTimeMillis();
            firstTime = false;
            System.out.println("Time to generate plan: " + (timeMilliStop - timeMilliStart) + " msec\n");
            System.out.println("Solution moves: \n\n");
            dirty = new HashSet<Point>();
        }

        if (path.isEmpty())
        {
           Point tgt = targets.remove(0);
           path = BFSPath.getPath(grid, pc.getLoc(), tgt);
        }

        Point next = path.remove(0);
        dirty.add(next);
        PacFace face = PacUtils.direction(pc.getLoc(), next);
        System.out.printf("%3d : From [ %2d, %2d ] go %s%n", ++simTime, pc.getLoc().x, pc.getLoc().y, face);

        return face;
    }
}