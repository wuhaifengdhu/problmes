package com.haifwu.zhangheng.recover;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.util.HashMap;

/**
 * Generic dic file based on excel text.
 *
 * @author Wu Haifeng
 * @CreateDate: 28/3/17
 */
public class GenerateDict {
    private String excel_input;
    private String dict_output;
    private HashMap<String,Integer> wordABCount = new HashMap<String,Integer>(); //count words A B times
    private HashMap<String,Integer> singleWordCount = new HashMap<String,Integer>(); // count word show up times
    private HashMap<String, Integer> aloneWordCount = new HashMap<>(); // count word alone appeared
    private HashMap<String,Double> proAB = new HashMap<String,Double>(); //record the probability of AB pro(AB) = count(A,B)/MAX(count(A*),count(*B))

    private GenerateDict(String excel_input, String dict_output){
        this.excel_input = excel_input;
        this.dict_output = dict_output;
    }

    private void countAB() throws IOException, InvalidFormatException {
        Workbook book = getExcelWorkbook(this.excel_input);
        Sheet sheet = book.getSheetAt(0);
        DataFormatter formatter = new DataFormatter();
        int lastRowNum = sheet.getLastRowNum();
        System.out.println("last number is "+ lastRowNum);
        for(int i = 1 ; i < lastRowNum ; i++){ // For each row, escape the first row
            Row row = sheet.getRow(i);
            if(row != null){
                int lastCellNum = row.getLastCellNum();
                Cell cell = null;
                for( int k = 0 ; k <= lastCellNum ; k++ ) {
                    cell = row.getCell(k);
                    if( cell != null ){
                        String cellValue = new String(formatter.formatCellValue(cell).getBytes("iso-8859-1"), "UTF-8");
                        String[] words = cellValue.split("\\s+");
                        for(int j = 0; j < words.length - 1; j++){
                            //1. Count each word
                            increaseHashMapCountValue(singleWordCount, words[j]);
                            //2. Count continue words
                            increaseHashMapCountValue(wordABCount, words[j]+ " " + words[j + 1]);
                        }
                        if(words.length > 0){
                            increaseHashMapCountValue(singleWordCount, words[words.length - 1]);
                        }
                        if(words.length == 1){
                            increaseHashMapCountValue(aloneWordCount, words[0]);
                        }
                    }
                }
            }
        }
    }

    private void insertAloneWordPro(){
        for (String key : aloneWordCount.keySet()) {
            this.proAB.put(key, this.aloneWordCount.get(key) * 1.0 / this.singleWordCount.get(key));
        }
    }

    private void increaseHashMapCountValue(HashMap<String, Integer> hashMap, String key){
        if(key.length() < 1) return;
        String lower_key = key.toLowerCase();
        if(hashMap.containsKey(lower_key)){
            hashMap.put(lower_key, hashMap.get(lower_key) + 1);
        } else {
            hashMap.put(lower_key, 1);
        }
    }


    //calculate the probability word A appears ,then B
    private void countProAB() throws IOException {
        for (String key : wordABCount.keySet()) {
            String[] words = key.split("\\s+");
            if(words.length != 2){
                System.out.println("Error happened when split " + key);
            } else {
                try{
                    // According to Naive Bayes: P(AB) = P(A)* P(B|A) = P(B) * P(A|B)
                    // P(A|B) = P(AB) / P(B); P(B|A) = P(AB) / P(A)
                    // Here we use max(p(B|A, p(A|B)）
                    double proA_B = wordABCount.get(key) * 1.0 / singleWordCount.get(words[1]);
                    double proB_A = wordABCount.get(key) * 1.0 / singleWordCount.get(words[0]);
                    this.proAB.put(key, Math.max(proA_B, proB_A));
                } catch (NullPointerException e){
                    System.out.println("NullPointerException happen when key is " + key);
                }
            }
        }
    }

    private void run() throws IOException, InvalidFormatException {
        countAB();          //calculate AB
        countProAB();       //calculate the probability
        insertAloneWordPro();  // For word alone insert into dic
        dumpProbability(this.proAB);
    }

    private void dumpProbability(HashMap<String, Double> hashMap) throws IOException {
        PrintWriter writer = new PrintWriter(this.dict_output, "ISO-8859-1");
        for(String key : hashMap.keySet()){
            writer.write(key + "\t" + hashMap.get(key) + "\n");
        }
        writer.close();
    }

    public static Workbook getExcelWorkbook(String filePath) throws IOException{
        Workbook book = null;
        File file  = null;
        FileInputStream fis = null;

        try {
            file = new File(filePath);
            if(!file.exists()){
                throw new RuntimeException("File not exist!");
            }else{
                fis = new FileInputStream(file);
                book = WorkbookFactory.create(fis);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            if(fis != null){
                fis.close();
            }
        }
        return book;
    }

    public static void main(String[] args) throws IOException, InvalidFormatException {
        if(args.length < 2){
            System.out.println("Usage: GenerateDic excel_file dict_file");
            return;
        }
        String excel_file = args[0], dict_file = args[1];
        GenerateDict convert = new GenerateDict(excel_file, dict_file);
        convert.run();
    }

}
