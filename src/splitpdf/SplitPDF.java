package splitpdf;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Jan Pogocki
 * Created on 07.10.2016.
 * Splits PDFs
 */

public class SplitPDF {
    private int counterToDo = 0;
    private int counterDone = 0;
    private int numberOfPages = 0;

    SplitPDF(String _path, String _pdfFilename) {
        countHowMuchToDo(_path, _pdfFilename);
    }

    public void execute(String _path, String _pdfFilename, double _percent){
        PDF2img(_path, _pdfFilename);

        for (int i=0; i<numberOfPages; i++)
            divideImg(_path + "/temp_splitpdf", i + ".png", _percent);

        combinePDF(_path);
    }

    private void countHowMuchToDo(String path, String pdfFilename){
        PDDocument doc = null;
        try {
            doc = PDDocument.load(new File(path + "/" + pdfFilename));
            numberOfPages = doc.getNumberOfPages();
            counterToDo = numberOfPages*4; //parse, divide, combine, combine
            doc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double progressStatus(){
        if (counterToDo == 0)
            return 0;
        else
            return counterDone/counterToDo;
    }

    private void PDF2img(String path, String pdfFilename){
        try {

            File dstFile = new File(path + "/temp_splitpdf");
            dstFile.mkdir();

            PDDocument document = PDDocument.load(new File(path + "/" + pdfFilename));
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            int pageCounter = 0;
            for (PDPage page : document.getPages())
            {
                BufferedImage bim = pdfRenderer.renderImageWithDPI(pageCounter, 100, ImageType.RGB);
                ImageIOUtil.writeImage(bim, path + "/temp_splitpdf/" + (pageCounter++) + ".png", 100);
                counterDone++;
            }
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void divideImg(String path, String imgFilename, double percent){
        File fileImg = new File(path + "/" + imgFilename);
        BufferedImage img = null;

        try {
            img = ImageIO.read(fileImg);
            int imgWidth = img.getWidth();
            int imgHeight = img.getHeight();

            BufferedImage img1 = img.getSubimage(0, 0, (int) (imgWidth*percent), imgHeight);
            BufferedImage img2 = img.getSubimage((int) (imgWidth*percent), 0, (int) (imgWidth*(1-percent)), imgHeight);

            String imgFilenameShort = imgFilename.split(".png")[0];
            ImageIO.write(img1, "png", new File(path + "/" + imgFilenameShort + "_1.png"));
            ImageIO.write(img2, "png", new File(path + "/" + imgFilenameShort + "_2.png"));
            fileImg.delete();
            counterDone++;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void combinePDF(String path){
        int numberOfFiles = new File(path + "/temp_splitpdf").list().length/2;
        int i = 0;

        PDDocument doc = new PDDocument();

        while (i < numberOfFiles){
            int j = 1;

            while (j < 3){
                try {
                    String pathToImg = path + "/temp_splitpdf/" + i + "_" + j + ".png";
                    InputStream in = new FileInputStream(pathToImg);
                    BufferedImage bimg = ImageIO.read(in);
                    float width = bimg.getWidth() ;
                    float height = bimg.getHeight();

                    PDPage page = new PDPage(new PDRectangle(width, height));
                    doc.addPage(page);

                    PDImageXObject pdImageXObject = LosslessFactory.createFromImage(doc, bimg);
                    PDPageContentStream contentStream = new PDPageContentStream(doc, page);
                    contentStream.drawImage(pdImageXObject, 0, 0);
                    contentStream.close();

                    new File(pathToImg).delete();

                    counterDone++;
                    j++;
                } catch (IOException e){
                    e.printStackTrace();
                }
            }

            i++;
        }

        try {
            doc.save(new File(path + "/nowy.pdf"));
            doc.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

  /*  public static void main(String[] args){
        //PDF2img("D:\\Pobrane", "pdf.pdf");
        //divideImg("D:\\Pobrane\\temp_splitpdf", "6.png", 0.5);
        //combinePDF("D:\\Pobrane");
    }*/
}
