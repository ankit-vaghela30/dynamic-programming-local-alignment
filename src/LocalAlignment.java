import java.io.File;
import java.util.*;
/**
 * @author: Ankit Vaghela
 * Search a large text for a query using Local alignment
 * solved by using Dynamic programming approach
 */
public class LocalAlignment{
    public static final boolean DEBUG = true;
    // query
    public String query = "";

    // large text 
    public String largeTextString;

    // k top highest scores will be traced
    public int k;

    // Map which contains score scheme
    public Map<String, Integer> scoreScheme = new HashMap<String, Integer>();

    // Matrix containing dynamic programming table
    public Integer[][] dpTable;

    // Matrix containing copy of dynamic programming table
    public Integer[][] dpTableCopy;
    
    // traceback map
    public Map<String, Integer[]> traceBackMap = new HashMap<String, Integer[]>();

    // Create score scheme from the raw text
    public void createScoreScheme(String scoreSchemeText){
        for(String scoreLine: scoreSchemeText.split("\n")){
            this.scoreScheme.put(scoreLine.split(":")[0], Integer.parseInt(scoreLine.split(":")[1]));
        }
    }

    public LocalAlignment(String[] args){
        Scanner sc;
        try{
            File scoreSchemeFile = new File(args[2]);
            sc = new Scanner(scoreSchemeFile);
            String scoreSchemeText = readFile(sc);
            createScoreScheme(scoreSchemeText);

            // query 
            this.query = this.query + args[3];
            for(int i = 4; i < args.length; i++){
                this.query = this.query +" "+ args[i];
            }
            
            // number k
            this.k = Integer.parseInt(args[1]);

            // extract text from large text file
            File largeTextFile = new File(args[0]);
            sc = new Scanner(largeTextFile);
            this.largeTextString = readFile(sc);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    // convert file to raw text
    public String readFile(Scanner sc){
        String text = "";
        while(sc.hasNextLine()){
            text = text + sc.nextLine() + "\n";
        }
        // Remove last "\n"
        return text.substring(0, text.length()-1);
    }

    //set initial DP table
    public void createInitialDPTable(Integer[][] dpTable){
        for(int i = 0; i < dpTable.length; i++){
            for (int j = 0; j < dpTable[i].length; j++){
                dpTable[i][j] = 0;
            }
        }
    }

    // Utility function to print dynamic programming table
    public void printDPTable(){
        for(int i = 0; i < dpTable.length; i++){
            for (int j = 0; j < dpTable[i].length; j++){
                if(dpTable[i][j] == 0){
                    System.out.print(dpTable[i][j] + "   |");
                }else if(dpTable[i][j].toString().length() == 2){
                    System.out.print(dpTable[i][j] + "  |");
                }else{
                    System.out.print(dpTable[i][j] + " |");
                }
            }
            System.out.print("\n");
        }
    }

    // Utility function to print traceback map
    public void printTraceBackMap(){
        for(String pos : this.traceBackMap.keySet()){
            System.out.print(pos+" : ");
            System.out.println(this.traceBackMap.get(pos)[0]+", "+this.traceBackMap.get(pos)[1]);
        }
    }

    // reverse the string
    public String reverseString(String strToReverse){
        String reversedStr = "";
        for (int i = strToReverse.length()-1; i >=0 ; i--){
            reversedStr = reversedStr + strToReverse.charAt(i);
        }
        return reversedStr;
    }

    // Create dynamic programming table
    public void createDynamicProgrammingTable(){
        // Set base case
        this.dpTable = new Integer[this.query.length()+1][this.largeTextString.length()+1];
        createInitialDPTable(this.dpTable);

        // create DP table
        for(int i = 1; i < dpTable.length; i++){
            for (int j = 1; j < dpTable[i].length; j++){
                if(this.query.charAt(i-1) == this.largeTextString.charAt(j-1)){
                    this.dpTable[i][j] = this.dpTable[i-1][j-1] + this.scoreScheme.get("match");
                    this.traceBackMap.put(i+","+j, new Integer[]{i-1, j-1});
                } else{
                    int insertion = this.dpTable[i-1][j] + this.scoreScheme.get("insertion");
                    int deletion = this.dpTable[i][j-1] + this.scoreScheme.get("deletion");
                    if(insertion > 0 && insertion >= deletion){
                        this.traceBackMap.put(i+","+j, new Integer[]{i-1, j});
                    } else if(deletion > 0 && deletion > insertion){
                        this.traceBackMap.put(i+","+j, new Integer[]{i, j-1});
                    }
                    this.dpTable[i][j] = Math.max(0, Math.max(insertion, deletion));
                }
            }
        }
        //Create a copy so that task of finding multiple max is facilitated
        this.dpTableCopy = new Integer[this.query.length()+1][this.largeTextString.length()+1];
        for(int i = 0; i < dpTable.length; i++){
            for (int j = 0; j < dpTable[i].length; j++){
                dpTableCopy[i][j] = dpTable[i][j];
            }
        }
    }

    // Align two texts
    public String alignTwoText(String query, String largeText, int queryStart, int queryEnd, int largeTextStart, int largeTextEnd){
        
        // prfix length used to determine which query of large text to keep fixed 
        int prefixLength = 0;
        
        // Create the segment of the large text 
        //go backwards on text
        for(int i = largeTextStart-2; i>=0; i--){
            if(this.largeTextString.charAt(i) == ' '){
                // We have to update large text start index as we now have just a word
                largeTextStart = largeTextStart-1-i;
                break;
            }
            largeText = this.largeTextString.charAt(i) + largeText;
            prefixLength++;
        }
        
        // go forward on text
        for(int i = largeTextEnd; i<this.largeTextString.length(); i++){
            if(this.largeTextString.charAt(i) == ' '){
                break;
            }
            largeText = largeText + this.largeTextString.charAt(i);
        }
        
        // keep large text as is align query accordingly
        if(prefixLength > String.valueOf(queryStart).length()+2){
            for(int i = 0; i < largeText.length(); i++){
                if(i < (largeTextStart - 1 - String.valueOf(queryStart).length() - 2)){
                    query = " " + query;
                }
            }
        }
        
        // keep the query as is and align large text accordingly
        else{
            for(int i = 0; i < query.length(); i++){
                if(i < (queryStart - 1 + String.valueOf(queryStart).length() + 2 - (largeTextStart-1))){
                    largeText = " " + largeText;
                }
            }
        }
        return (query + "\n" +largeText);
    }

    // Function that prints path and final result
    public void extractMaxScoreAndPrintPath(){
        int maxNum = 0;

        // Find max number
        Integer[] maxNumLoc = new Integer[2];
        for(int i = 0; i < dpTableCopy.length; i++){
            for (int j = 0; j < dpTableCopy[i].length; j++){
                if(dpTableCopy[i][j] > maxNum){
                    maxNum = dpTableCopy[i][j];
                    maxNumLoc = new Integer[]{i, j};
                }
            }
        }
        
        // Store initial max number location so that we can make it 0 in DP table
        Integer[] initMaxNumLoc = new Integer[]{maxNumLoc[0], maxNumLoc[1]};
        System.out.println("Score is: "+maxNum);

        if(maxNum != 0){
            String queryStr = "";
            String bigStr = "";
            String endIndexForQuery = "";
            String endIndexForText = "";
            
            // For first character 
            if(this.query.charAt(maxNumLoc[0]-1) == this.largeTextString.charAt(maxNumLoc[1]-1)){
                queryStr = ""+this.query.charAt(maxNumLoc[0]-1);
                bigStr = ""+this.largeTextString.charAt(maxNumLoc[1]-1);
            }else if(this.dpTable[maxNumLoc[0]][maxNumLoc[1]-1] != 0){
                queryStr = queryStr+"-";
                bigStr = bigStr + this.largeTextString.charAt(maxNumLoc[1]-1);
            } else if(this.dpTable[maxNumLoc[0]-1][maxNumLoc[1]] != 0){
                queryStr = queryStr + this.query.charAt(maxNumLoc[0]-1);
                bigStr = bigStr+"-";
            }
            endIndexForQuery = String.valueOf(maxNumLoc[0]);
            endIndexForText = String.valueOf(maxNumLoc[1]);

            // Do recursively for rest query characters
            while(this.traceBackMap.get(maxNumLoc[0]+ "," +maxNumLoc[1]) != null && this.dpTable[this.traceBackMap.get(maxNumLoc[0]+ "," +maxNumLoc[1])[0]][this.traceBackMap.get(maxNumLoc[0]+ "," +maxNumLoc[1])[1]] != 0){
                maxNumLoc = this.traceBackMap.get(maxNumLoc[0]+ "," +maxNumLoc[1]);
                if(this.query.charAt(maxNumLoc[0]-1) == this.largeTextString.charAt(maxNumLoc[1]-1)){
                    //System.out.println("reward");
                    queryStr = queryStr + this.query.charAt(maxNumLoc[0]-1);
                    bigStr = bigStr + this.largeTextString.charAt(maxNumLoc[1]-1);
                } else if(this.dpTable[maxNumLoc[0]][maxNumLoc[1]-1] != 0){
                    //System.out.println("deletion");
                    queryStr = queryStr+"-";
                    bigStr = bigStr + this.largeTextString.charAt(maxNumLoc[1]-1);
                } else if(this.dpTable[maxNumLoc[0]-1][maxNumLoc[1]] != 0){
                    //System.out.println("insertion");
                    queryStr = queryStr + this.query.charAt(maxNumLoc[0]-1);
                    bigStr = bigStr+"-";
                } else{
                    break;
                }
            }
            String startIndexForquery = String.valueOf(maxNumLoc[0]);
            String startIndexForText = String.valueOf(maxNumLoc[1]);
            String displayStr = "";
            
            // reverse the strings
            queryStr = reverseString(queryStr);
            queryStr = "[" + startIndexForquery + "]" + queryStr + "[" + endIndexForQuery + "]";
            bigStr = reverseString(bigStr);
            for(int i = 0; i < ("[" + startIndexForquery + "]").length(); i++){
                bigStr = " " + bigStr;
            }
            
            // prints final output
            //System.out.println(this.alignTwoText(queryStr, bigStr, Integer.valueOf(startIndexForquery), Integer.valueOf(endIndexForQuery), Integer.valueOf(startIndexForText), Integer.valueOf(endIndexForText)));
            System.out.println(queryStr);
            System.out.println(bigStr);
            this.dpTableCopy[initMaxNumLoc[0]][initMaxNumLoc[1]] = 0;
        }else{
            System.out.println("Everything is 0!");
        }
        // put 0 at current highest
    }

    /**
     * main method which accepts four args
     * 1. A file containing large text sequence
     * 2. short query text
     * 3. number k: k top highest scores will be traced
     * 4. A text file containing score scheme
     */
    public static void main(String[] args) {
        LocalAlignment localAlignment = new LocalAlignment(args);
        try{
            if(DEBUG){
                System.out.println(localAlignment.query);
                System.out.println(localAlignment.k);
                System.out.println(localAlignment.largeTextString);
            }
            
            // Create Dynamic programming table
            localAlignment.createDynamicProgrammingTable();
            if(DEBUG){
                localAlignment.printDPTable();
                localAlignment.printTraceBackMap();
            }

            // extract k maximum paths
            //System.out.println("--------------------------------------------------------");
            for(int i = 0; i < localAlignment.k; i++){
                System.out.println("--------------------------------------------------------");
                System.out.println("hit : "+(i+1));
                System.out.println();
                localAlignment.extractMaxScoreAndPrintPath();
                System.out.println("--------------------------------------------------------");
            }
            //System.out.println("--------------------------------------------------------");
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}