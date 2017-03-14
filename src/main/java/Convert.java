/**
 * Created by hfwu on 16/2/17.
 */
import com.google.common.io.Resources;

import java.io.*;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;

public class Convert {
    private static final String dicFile = "probability.dic";

    public static void main(String[] args) throws IOException {
        Map<String, Integer> single_dic = new HashMap<String, Integer>(5000);
        Map<String, Integer> double_dic = new HashMap<String, Integer>(5000);
        String segment_file  = Resources.getResource("segment.txt").getFile();
        BufferedReader br = new BufferedReader(new FileReader(segment_file));
        int count = 0;
        for(String line; (line = br.readLine())!= null;){
            count += 1;
            String[] words= line.split("\t");

            List<String> words_list = new ArrayList<String>();
            for(int i = 0; i < words.length; i++){
                if(words[i] != null && words[i].trim().length() > 0){
                    words_list.add(words[i].trim());
                }
            }

            for(int i = 0; i < words_list.size() - 1; i++){
                if(single_dic.containsKey(words_list.get(i))){
                    single_dic.put(words_list.get(i), single_dic.get(words_list.get(i)) + 1);
                } else {
                    single_dic.put(words_list.get(i), 1);
                }
                String pair = words_list.get(i) + " " + words_list.get(i + 1);
                if(double_dic.containsKey(pair)){
                    double_dic.put(pair, double_dic.get(pair) + 1);
                } else {
                    double_dic.put(pair, 1);
                }
            }

            if(words_list.size() > 0){
                String lastWord = words_list.get(words_list.size() - 1);
                if(single_dic.containsKey(lastWord)){
                    single_dic.put(lastWord, single_dic.get(lastWord) + 1);
                } else {
                    single_dic.put(lastWord, 1);
                }
            }
            if(count % 10000 == 0){
                System.out.println(count);
            }
        }
        System.out.println("Finished Reading all the lines!");



        PrintWriter out = new PrintWriter(dicFile);
        DecimalFormat df = new DecimalFormat("#.######");
        Iterator it = double_dic.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry pair = (Map.Entry) it.next();
            String[] words = pair.getKey().toString().split(" ");
            if(words.length != 2){
                System.out.println("Error parse double dic key: " + pair.getKey().toString());
                continue;
            }
            try {
                if(pair.getValue() != null){
                    double probability = Integer.valueOf(pair.getValue().toString()) * 1.0 / (single_dic.get(words[0]) +
                            single_dic.get(words[1]));
                    out.println(pair.getKey() + "\t" + df.format(probability));
                } else if(pair.getKey() != null){
                    System.out.println("Empty value for key: " + pair.getKey());
                } else {
                    System.out.println("Both key and value is null");
                }
            } catch (NullPointerException x){
                System.out.print(x);
            }

            it.remove();
        }
        out.close();
    }

}
