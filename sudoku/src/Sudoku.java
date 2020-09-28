import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
//Alain Kouassi
/* I tried to create a solver without watching the sudoku video
   had I watched that Before coding, it probably would have saved me time
   and my solution would look a little different.
 */

public class Sudoku {

    private static int boardSize = 0;
    private static int partitionSize = 0;
    private static int[][] vals = null;
    private static int rows=0;
    private static int cols=0;

    public static void main(String[] args) throws IOException {
        String filename = args[0];
        File inputFile = new File(filename);
        Scanner input = null;

        int temp = 0;
        int count = 0;

        try {
            input = new Scanner(inputFile);
            temp = input.nextInt();
            boardSize = temp;
            partitionSize = (int) Math.sqrt(boardSize);
            System.out.println("Boardsize: " + temp + "x" + temp);
            vals = new int[boardSize][boardSize];

            System.out.println("Input:");
            int i = 0;
            int j = 0;
            while (input.hasNext()){
                temp = input.nextInt();
                count++;
                System.out.printf("%3d", temp);
                vals[i][j] = temp;
                if (temp == 0) {
                    // TODO
                    // wanted to leave empty spaces as 0s
                }
                j++;
                if (j == boardSize) {
                    j = 0;
                    i++;
                    System.out.println();
                }
                if (j == boardSize) {
                    break;
                }
            }
            input.close();
        } catch (FileNotFoundException exception) {
            System.out.println("Input file not found: " + filename);
        }
        if (count != boardSize*boardSize) throw new RuntimeException("Incorrect number of inputs.");
        rows = vals.length;
        cols = vals[0].length;

        boolean solved = solve();

        // Output
        String outputName = fileNameChange(filename);
        File outputFile = new File(outputName);
        FileWriter writer = new FileWriter(outputName);
        PrintWriter printWriter = new PrintWriter(writer);
        if (!solved) {
            System.out.println("No solution found.");
            printWriter.println("-1");
            printWriter.close();
            writer.close();
            return;
        }
        System.out.println("\nOutput\n");

        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                System.out.printf("%3d", vals[i][j]);
                printWriter.printf("%3d", vals[i][j]);
            }
            System.out.println();
            printWriter.println();
        }
        printWriter.close();
        writer.close();
        System.out.printf("\nSudoku Solution in %s", outputName);

    }
    public static String fileNameChange(String fileName){
        //add solution to the filename
        int dotIndex = fileName.lastIndexOf(".");
        if(dotIndex == -1){
            return fileName+"Solution";
        }
        else return fileName.substring(0, dotIndex)+ "Solution"+ fileName.substring(dotIndex);
    }

    public static boolean solve(){
        int row= 0;
        int col=0;
        // find a position to solve
        int[] a = findLocation(row, col);
        // if all cells assigned then sudoku is solved
        if(a[0] == 0)
            return true;
        row = a[1];
        col = a[2];

        for(int i =1; i<=rows; i++){
            //check if a value can be added
            if(safe(i, row, col)){
                //value is safe to insert, insert
                vals[row][col] = i;
                //recurse to see if working solution found
                if(solve())
                    return true;
                //this insert does not lead to a working solution, reset
                vals[row][col] =0;
            }
        }
        //no working solutions found
        return false;
    }
    public static boolean safe(int val, int row, int col){
        //check rows for only 1 instance of each number
        for(int i =0; i< rows; i++){
            if(vals[row][i] == val)
                return false;
        }
        //check columns for only 1 instance of each number
        for(int j = 0; j<cols; j++){
            if(vals[j][col] == val)
                return false;
        }
        int rowSection = (int) Math.sqrt(rows);
        int colSection = (int) Math.sqrt(cols);

        int rowStart = ((row/rowSection)*rowSection);
        int colStart = ((col/colSection)*colSection);

        //check subsections to make sure numbers are not reused.
        for(int i = rowStart; i<rowStart+rowSection; i++){
            for(int j = colStart; j<colStart+colSection; j++){
                if(vals[i][j] == val)
                    return false;
            }
        }
        return true;
    }

    // find empty positions
    public static int[] findLocation(int row, int col){
        int numbUnassigned = 0;
        for(int i =0; i< rows;i++){
            for(int j =0; j<cols; j++){
                if(vals[i][j] == 0){
                    row = i;
                    col = j;
                    numbUnassigned = 1;
                    int[] unassigned = {numbUnassigned, row, col};
                    return unassigned;
                }
            }
        }
        // no more empty positions
        int[] unassigned = {numbUnassigned, -1, -1};
        return unassigned;
    }
}