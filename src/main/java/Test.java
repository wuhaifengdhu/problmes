/**
 * Created by haifwu on 12/7/16.
 */
import java.io.*;
import java.util.*;
import jxl.*;
import jxl.read.biff.BiffException;
import jxl.write.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
public class Test{
    private static final String PATH1 = "/E:/datadealing/src/data2.xls";
    private static final String PATH2 = "/E:/datadealing/src/data3.xls";
    private static final String PATH3 = "/E:/datadealing/src/data3.xls";

    private static HashMap<String,Integer> hashmap = new HashMap<String,Integer>(); //record the number of AB
    private static HashMap<String,Integer> A_and_B = new HashMap<String,Integer>(); // record the number of A* and *B;
    private static HashMap<String,Double> proAB = new HashMap<String,Double>(); //record the probability of AB pro(AB) = count(A,B)/MAX(count(A*),count(*B))

    public static void main(String[] args) throws IOException, BiffException, WriteException {
        // change();
        //  getData();
        training();
    }
    //将数据打乱
    public static void change() throws IOException, BiffException, WriteException{
        // Temp variable name
        int row, colum1, colum2;
        Cell cell1, cell2;

        // Sheet sheet;
        Workbook wb = Workbook.getWorkbook(new File(PATH1));
        WritableWorkbook wwb = Workbook.createWorkbook(new File(PATH2), wb);
        WritableSheet sheet = wwb.getSheet(0);

        Random random = new Random(); // Put random object out of for loop
        for (int i = 0; i < 8000; i++) {   //将8000条数据交换
            //Generate row, column1, column2
            row = random.nextInt(42539);
            // row = random.nextInt(6);
            colum1 = random.nextInt(56);
            // colum1 = random.nextInt(2);
            do {
                colum2 = random.nextInt(56);
                // colum2 = random.nextInt(2);
            } while (colum1 == colum2);
            //   System.out.println("row = " + row + " colum1 = " + colum1 + " colum2 = " + colum2);

            //Swap content of this two cell
            cell1 = sheet.getCell(colum1, row);//（列，行）
            cell2 = sheet.getCell(colum2, row);
            if(i < 5000) {
                //   System.out.println("cell1: " + cell1.getContents() + "   cell2:" + cell2.getContents());
                if (cell1.getContents() != null && cell2.getContents() != null) {
                    sheet.addCell(new Label(colum1, row, cell2.getContents()));
                    sheet.addCell(new Label(colum2, row, cell1.getContents()));
                }
            }else{
                String temp1 = "";
                if(i < 6000){
                    temp1 = cell1.getContents() + ";" + cell2.getContents();
                }else if(i < 7000){
                    temp1 = cell1.getContents() + " " + cell2.getContents();
                }else{
                    temp1 = cell1.getContents() + "," + cell2.getContents();
                }


                System.out.println("temp1 = "+temp1);
                sheet.addCell(new Label(colum1, row,temp1));
                sheet.addCell(new Label(colum2,row,""));
            }
        }
        wwb.write();
        wwb.close();
        wb.close();
    }
    //将表格中的数据全部取出，放入dataset.txt中
    public static void getData() throws IOException, BiffException{
        String PATH3 = "/E:/datadealing/src/data3.xls";
        String PATH4 = "/E:/datadealing/src/dataset.txt";
        Workbook wb = Workbook.getWorkbook(new File(PATH3));
        //  WritableWorkbook wwb = Workbook.createWorkbook(new File(PATH4), wb);
        Sheet sheet = wb.getSheet(0);
        FileWriter writer = new FileWriter(PATH4);
        String cell;
        for(int i = 2; i < 42540 ; i++){
            for(int j = 0 ; j < 57; j++){
                cell = sheet.getCell(j ,i).getContents().toString();
                System.out.println(cell);
                writer.write(cell+';');
            }
            writer.write("\r\n");
        }
        writer.close();
    }

    public static void countAB () throws IOException,FileNotFoundException,BiffException{
        Workbook wb = Workbook.getWorkbook(new File(PATH3));
        Sheet sheet = wb.getSheet(0);
        ArrayList<String> list = new ArrayList<String>();  //Arraylist存取数据可以动态添加，到时候记录一行的数据后清空供第二行使用
        list.clear();
        for(int i = 2 ; i < 42540 ; i++){ //放入hashmasp中连续连个单词及其计数
            for (int k = 0 ; k < 57; k++){  //将每一行的数据单词拆开，放入list中
                String[] str = sheet.getCell(k,i).getContents().trim().split(" |,|;"); //trim可以去掉首位的空格
                //   System.out.println("++++++"+sheet.getCell(k,2).getContents());
                for(int m = 0 ; m < str.length; m++){
                    str[m].replace(" ","");
                    /*if(str[m] == ""){
                        System.out.println("null++++");
                    }*/
                    if(str[m].length() > 0){   // ????? 之前用的str[m] ！= "" 都不成功，用了这个就成功了
                        list.add(str[m]);
                    }
                }
                list.add(";");   //给每个单元格后边加一个分号，以免前一个单元格的最后一个单词和后一个单元格的第一个单词产生联系
            }

            for(int s = 0 ; s < list.size()-1; s++){
                // System.out.println(list.get(s));
                String temp = list.get(s).toString()+" "+list.get(s+1).toString();  // 将list中连续两个单词连起来，中间用空格分开，插入到hash表中
                if(hashmap.containsKey(temp)){
                    Integer value = hashmap.get(temp) + 1;
                    hashmap.put(temp,value);
                }else{
                    hashmap.put(temp,1);
                }
            }
            list.clear();
        }

    }

    public static void initial(){
        hashmap.clear();
        A_and_B.clear();
        proAB.clear();
    }
    //count A * , record it in hashmap A_and_B
    public static void countA_and_B() throws IOException,FileNotFoundException,BiffException{
        Set<String> set = new HashSet<String>();   //用来存储所有的单词
        set.clear();
        Iterator<String> keySetIterator = hashmap.keySet().iterator();
        while (keySetIterator.hasNext()) {
            String key = keySetIterator.next();
            //   System.out.println("key: "+key+"  value:  "+hashmap.get(key));
            String[] temp = key.trim().split(" ");
            for(String s:temp){
                set.add(s);
            }
        }


        int countA_ = 0, count_B = 0; //record the count of A* and *B
        for(String ss : set){
            Iterator<String> keySetIterator2 = hashmap.keySet().iterator();  //?????在里面定义，时间很长，还没出来，定义在外面，找不到ss
            // System.out.println("ss ******" +ss);
            while (keySetIterator2.hasNext()){
                String key = keySetIterator2.next();
                String[] temp = key.split(" ");

                if(temp[0].trim().equals(ss)){
                    System.out.println("ss1 = "+ss+";temp[0] = "+temp[0]); //???????输出也很奇怪，不知道哪里出错了
                    countA_ += hashmap.get(key);  //if it is the first word, then add the value to the count A*
                }
                if(temp[1].trim().equals(ss)){
                    System.out.println("ss2 = "+ss+";temp[1] = "+temp[1]);
                    count_B += hashmap.get(key); //if it is the second word, then add the value to the count *B
                }
            }
            countA_ = 0;
            count_B = 0;
            String s1 = ss + " *";
            String s2 = "* "+ss;
            A_and_B.put(s1,countA_);
            A_and_B.put(s2,count_B);
        }

        Iterator<String> In = A_and_B.keySet().iterator();
        while(In.hasNext()){
            String key = In.next();
            int value = A_and_B.get(key);
            System.out.println("in A_and_B ; KEY = "+key+"  , VALUE = "+value);
        }

    }

    //caculate the probability word A appears ,then B
    public static void countProAB() throws IOException,FileNotFoundException,BiffException {
        Iterator<String> keySetIterator = hashmap.keySet().iterator();
        while(keySetIterator.hasNext()){
            String key = keySetIterator.next();
            String[] temp = key.trim().split(" ");
            String str1 = temp[0]+" *";
            String str2 = "* "+temp[1];

            int count12 = hashmap.get(key);
            int count1 = A_and_B.get(str1);
            int count2 = A_and_B.get(str2);

            double probality = ProBefore(count12,count1,count2);
            proAB.put(key,probality);
            System.out.println("after calculate the probality, key = "+key+"  value = "+probality);
        }

    }
    public static void training() throws IOException,FileNotFoundException,BiffException{

        initial();
        countAB(); //caculate AB
        countA_and_B();  //calulate A* and *B
        countProAB(); //caculate the probality
      /*  for(String ss:set){
            System.out.println(ss);
        }*/

    }
    public static  int min(int a, int b){
        if(a <= b){
            return a;
        }else{
            return b;
        }
    }

    //the probability word1 before word2
    public static  double  ProBefore(int count12, int count1, int count2){
        int temp = min(count1,count2);
        return count12*1.0/temp;
    }

}
