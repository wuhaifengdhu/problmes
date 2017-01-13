/**
 * Created by haifwu on 12/7/16.
 */
import com.google.common.io.Resources;
import jxl.Cell;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Random;
public class RandomSet{
    private static final String originFilename = "data.xls";
    private static final String newFileName = "data_update.xls";
    public static void main(String[] args) throws IOException, BiffException, WriteException, URISyntaxException {
        change();
    }

    public static void change() throws IOException, BiffException, WriteException, URISyntaxException {
        // Temp variable name
        int row, colum1, colum2;
        Cell cell1, cell2;

        // Sheet sheet;
        Workbook wb = Workbook.getWorkbook(new File(Resources.getResource(originFilename).getFile()));
        WritableWorkbook wwb = Workbook.createWorkbook(new File(newFileName), wb);
        WritableSheet sheet = wwb.getSheet(0);

        Random random = new Random(); // Put random object out of for loop
        for (int i = 0; i < 8000; i++) {   //将8000条数据交换
            //Generate row, column1, column2
            row = random.nextInt(42539);
            colum1 = random.nextInt(56);
            do {
                colum2 = random.nextInt(56);
            } while (colum1 == colum2);
            System.out.println("row = " + row + " colum1 = " + colum1 + " colum2 = " + colum2);

            //Swap content of this two cell
            cell1 = sheet.getCell(colum1, row);//（列，行）
            cell2 = sheet.getCell(colum2, row);
            System.out.println("cell1: " + cell1.getContents() + "   cell2:" + cell2.getContents());
            if(cell1.getContents() != null && cell2.getContents() != null){
                sheet.addCell(new Label(colum1, row, cell2.getContents()));
                sheet.addCell(new Label(colum2, row, cell1.getContents()));
            }
            System.out.println("Finished for the " + i + " times loop!");
        }
        wwb.write();
        wwb.close();
        wb.close();
    }
}
