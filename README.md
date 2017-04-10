1, Build the jar file   
Project Home> mvn clean package

This will generate a dic_generate.jar under target folder, you can copy it with the folder dependency-jars to
 anywhere for use.   

2, Use generated jar file   
Project Home> java -jar target/dic_generate.jar src/main/resources/test.xls  generic.dic

  



