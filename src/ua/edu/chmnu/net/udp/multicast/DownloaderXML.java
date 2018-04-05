package ua.edu.chmnu.net.udp.multicast;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is the abstraction of single download thread
 */
public class DownloaderXML implements Runnable {

    private final URL srcUrl;
    private final String srcFileName;
    private final String destDir;
    private int bufferSize = 64;
    private boolean active;
    private Thread workThread = null;
    private boolean status = false;

    /**
     * Constructor of download thread
     *
     * @param url - source file URL
     * @param destDir - destination directory
     * @param fileName - filename
     * @throws java.net.MalformedURLException
     */
    public DownloaderXML(String url, String destDir, String fileName) throws MalformedURLException {
        this.srcUrl = new URL(url);
        this.destDir = destDir;
        int idx = srcUrl.getFile().lastIndexOf('/');
        if (idx < 0) {
            throw new MalformedURLException(url);
        }
        this.srcFileName = fileName;
        this.active = false;
        checkDestDir();
    }

    /**
     * Constructor of download thread
     *
     * @param url - source file URL
     * @param destDir - destination directory
     * @param bufferSize - size of buffer to download
     * @param fileName - fileName
     * @throws java.net.MalformedURLException
     */
    public DownloaderXML(String url, String destDir, int bufferSize, String fileName) throws MalformedURLException {
        this(url, destDir, fileName);
        this.bufferSize = bufferSize;
    }

    /**
     * Checks existing of destination directory
     */
    private void checkDestDir() {
        if (!new File(destDir).exists()) {
            new File(destDir).mkdirs();
        }
    }

    public String getSrcUrl() {
        return srcUrl.toString();
    }

    public String getDestDir() {
        return destDir;
    }


    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public boolean isActive() {
        return active;
    }

    public Thread getWorkThread() {
        return workThread;
    }
    
    public void start() {                
        active = true;
        workThread = new Thread(this);
        workThread.start();
    }

    public void terminate() {
        active = false;
    }
    
    public boolean getStatus(){
        return status;
    }

    protected void downloadFromStream() throws IOException, InterruptedException {
        String destPath = destDir + File.separator + srcFileName;
        try (InputStream in = new BufferedInputStream(srcUrl.openStream());
                OutputStream out = new BufferedOutputStream(new FileOutputStream(destPath));) {

            byte[] buffer = new byte[this.bufferSize];
            int count, readed = 0;
            final long total = srcUrl.openConnection().getContentLength();
            while ((count = in.read(buffer)) > 0 && active) {
                readed += count;
                out.write(buffer, 0, count);
            }
        }
        status = true;
        System.out.println("Download XML the end..");
    }
    
    public String getParseStr() {
        String str = "";
        if(status){
            try {
             File inputFile = new File("D:\\Temp\\Curs.xml");
             DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
             DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
             Document doc = dBuilder.parse(inputFile);
             doc.getDocumentElement().normalize();
             NodeList nList = doc.getElementsByTagName("exchangerate");

             for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                   Element eElement = (Element) nNode;
                   str += eElement.getAttribute("ccy") + "==>" + eElement.getAttribute("base_ccy") 
                           + "\n" + eElement.getAttribute("buy") + "\n" + eElement.getAttribute("sale") + "\n\n";   
                }
             }
            } catch (Exception e) {
             e.printStackTrace();
            }
        }else{
            str = "Подождите идет загрузка XML";
        }
        return str;
    }

    @Override
    public void run() {
        try {
            downloadFromStream();
        } catch (IOException ex) {
            Logger.getLogger(MultiCastSenderApp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(DownloaderXML.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
