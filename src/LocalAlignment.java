import java.io.File;
import java.util.*;

public class LocalAlignment{

    // query
    public String query;

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
            File scoreSchemeFile = new File(args[3]);
            sc = new Scanner(scoreSchemeFile);
            String scoreSchemeText = readFile(sc);
            createScoreScheme(scoreSchemeText);

            // query 
            this.query = args[1];

            // number k
            this.k = Integer.parseInt(args[2]);

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

    public void printDPTable(){
        for(int i = 0; i < dpTable.length; i++){
            for (int j = 0; j < dpTable[i].length; j++){
                if(dpTable[i][j] == 0){
                    System.out.print(dpTable[i][j] + "  |");
                }else{
                    System.out.print(dpTable[i][j] + " |");
                }
            }
            System.out.print("\n");
        }
    }

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
                    if(insertion > 0 && insertion > deletion){
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
        String beginQuery = "[" + String.valueOf(queryStart) + "]";
        String endQuery = "[" + String.valueOf(queryEnd) + "]";
        if(largeText.length() > query.length()){
            for(int i = 0; i < largeText.length(); i++){
                if(i < largeTextStart-1){
                    query = " " + query;
                } else if(i > largeTextEnd-1){
                    query = query + " ";
                }
            }
        }
        /* add the start index
        for(int i = beginQuery.length()-1; i >= 0; i--){
            query = beginQuery.charAt(i) + query;
            largeText = " " + largeText;
        }

        // add the end text
        for(int i = 0; i < endQuery.length(); i++){
            query = query + endQuery.charAt(i);
            largeText = largeText + " ";
        }*/
        return (query + "\n" +largeText);
    }

    public void extractMaxScoreAndPrintPath(){
        int maxNum = 0;
        Integer[] maxNumLoc = new Integer[2];
        for(int i = 0; i < dpTableCopy.length; i++){
            for (int j = 0; j < dpTableCopy[i].length; j++){
                if(dpTableCopy[i][j] > maxNum){
                    maxNum = dpTableCopy[i][j];
                    maxNumLoc = new Integer[]{i, j};
                }
            }
        }
        Integer[] initMaxNumLoc = new Integer[]{maxNumLoc[0], maxNumLoc[1]};
        //System.out.println(this.dpTable[5][5]);
        if(maxNum != 0){    
            String queryStr = ""+this.query.charAt(maxNumLoc[0]-1);
            String bigStr = ""+this.largeTextString.charAt(maxNumLoc[1]-1);
            String endIndexForQuery = String.valueOf(maxNumLoc[0]);
            String endIndexForText = String.valueOf(maxNumLoc[1]);
            while(this.traceBackMap.get(maxNumLoc[0]+ "," +maxNumLoc[1]) != null && this.dpTable[this.traceBackMap.get(maxNumLoc[0]+ "," +maxNumLoc[1])[0]][this.traceBackMap.get(maxNumLoc[0]+ "," +maxNumLoc[1])[1]] != 0){
                Integer[] prevNumLoc = new Integer[]{maxNumLoc[0], maxNumLoc[1]};
                maxNumLoc = this.traceBackMap.get(maxNumLoc[0]+ "," +maxNumLoc[1]);
                if(this.query.charAt(maxNumLoc[0]-1) == this.largeTextString.charAt(maxNumLoc[1]-1)){
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
            queryStr = reverseString(queryStr);
            queryStr = "[" + startIndexForquery + "]" + queryStr + "[" + endIndexForQuery + "]";
            bigStr = reverseString(bigStr);
            //go backwards on text
            for(int i = Integer.valueOf(startIndexForText)-2; i>=0; i--){
                if(this.largeTextString.charAt(i) == ' '){
                    break;
                }
                bigStr = this.largeTextString.charAt(i) + bigStr;
            }
            // go forward on text
            for(int i = Integer.valueOf(endIndexForText); i<this.largeTextString.length(); i++){
                if(this.largeTextString.charAt(i) == ' '){
                    break;
                }
                bigStr = bigStr + this.largeTextString.charAt(i);
            }
            // align
            for(int i =0; i < ("[" + startIndexForquery + "]").length(); i++){
                bigStr = " "+bigStr;
            }
            //System.out.println(queryStr);
            //System.out.println(bigStr);
            System.out.println(this.alignTwoText(queryStr, bigStr, Integer.valueOf(startIndexForquery), Integer.valueOf(endIndexForQuery), Integer.valueOf(startIndexForText), Integer.valueOf(endIndexForText)));
            //System.out.println(queryStr);
            //System.out.println(bigStr);
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
            System.out.println(localAlignment.query);
            System.out.println(localAlignment.k);
            System.out.println(localAlignment.largeTextString);

            // Create Dynamic programming table
            localAlignment.createDynamicProgrammingTable();
            localAlignment.printDPTable();
            localAlignment.printTraceBackMap();

            // extract paths according to k
            for(int i = 0; i < localAlignment.k; i++){
                System.out.println("hit : "+i);
                localAlignment.extractMaxScoreAndPrintPath();
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}