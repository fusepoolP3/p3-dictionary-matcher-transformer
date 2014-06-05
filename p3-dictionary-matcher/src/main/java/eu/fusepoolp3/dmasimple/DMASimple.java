/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.fusepoolp3.dmasimple;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Test class for DMA
 * @author Gábor Reményi
 */
public class DMASimple {

    static String test = "Barack Hussein Obama II (Listeni/bəˈrɑːk huːˈseɪn oʊˈbɑːmə/; born August 4, 1961) is the 44th and current President of the United States, and the first African American to hold the office. Born in Honolulu, Hawaii, Obama is a graduate of Columbia University and Harvard Law School, where he served as president of the Harvard Law Review. He was a community organizer in Chicago before earning his law degree. He worked as a civil rights attorney and taught constitutional law at the University of Chicago Law School from 1992 to 2004. He served three terms representing the 13th District in the Illinois Senate from 1997 to 2004, running unsuccessfully for the United States House of Representatives in 2000.";
    
    static String test2 = "Tiszaderzs község Jász-Nagykun-Szolnok megye Tiszafüredi kistérségében."
                        + "Jász-Nagykun-Szolnok megyében, Tiszafüredtől délnyugatra, Abádszalóktól északkeletre, a Tisza mellett fekvő település."
                        + "Derzs már a honfoglalás után nemsokkal benépesült. Első ismert birtokosai a Tomaj nemzetség tagjai voltak, majd a 14. században rajtuk kívül még a Szalók nemzetség, a Derzsyek, a Losonczy, Kun és Pásztói valamint a Péntek (Cserőköz-puszta)és Borbély családok is birtokot szereztek itt."
                        + "A Péntek család a nemeslevelet II. Ferdinándtól 1631. évi jún. 18-án Péntek Demeter, neje Tóth Katalin, gyermekei Péter, Anna és Ilona, továbbá testvérei Lőrincz és Katalin nyerték s ugyanazon évben Borsodmegye hirdette ki. (Borsodm. lev. Pr. 5. f. 41.) 1699. évben Benedek, István, id. és ifj. András sarudi birtokosok voltak s 1709. évben igazolták nemességüket. (1709. év 486. jkl.) Az 1724. évi investigatió idején János, Demeter, Benedek, Lőrinc tiszaderzsi lakosok voltak, későbben pedig egyesek Kisújszállásra, Kúnhegyesre, Madarasra is elágaztak (1773. év 309. C. sz.)"
                        + "A 16. század közepe körül a Derzsyek mellett a Chemel, Fürök és Szabó családok, valamint Széky Pál egri hadnagy is - aki a Derzsy család rokonságához tartozott - birtokot szerzett Derzsen."
                        + "1670 után Széky Péter szendrői kapitány szerezte vissza nagyapja Széky Pál elzálogosított birtokát."
                        + "A Török hódoltság alatt a település kétszer is elnéptelenedett; 1566-ban a második tatár betöréskor, majd 1590-ben, de mindkét alkalommal hamarosan visszatelepült."
                        + "A Rákóczi-szabadságharc alatt a környéken folyó harcokban újra néptelenné vált, 1711-1712-ben népesedett be újra."
                        + "A felszabadító háborúk alatt a falu a kincstáré lett."
                        + "1745-ben Mária Terézia a falut Borbély Balázsnak adományozta."
                        + "A 20. század elején Jász-Nagykun-Szolnok vármegye Tiszai felső járásához tartozott."
                        + "2001-ben a település lakosságának 95%-a magyar, 5%-a cigány nemzetiségűnek vallotta magát.[3]";
    
    static String test3 = "Az első Őr Pr a Prség vízben Ŋaaa and Jaaa lerakni az Őrség Orbán Viktor.";
    
    static Annotation[] dict = { 
        new Annotation("Barack Hussein Obama","http://en.wikipedia.org/wiki/Barack_Obama", "Person"),    
        new Annotation("United States","http://en.wikipedia.org/wiki/United_States", "Location"),               
        new Annotation("President","http://en.wikipedia.org/wiki/President", "Misc"),                
        new Annotation("Illinois Senate", "http://en.wikipedia.org/wiki/Illinois_Senate", "Organization"),
        new Annotation("Honolulu", "http://en.wikipedia.org/wiki/Honolulu", "Location"),
        new Annotation("Hawaii", "http://en.wikipedia.org/wiki/Hawaii", "Location")
    };
    
    static List<Annotation> dict2;
    
    
    static Annotation[] dict3 = { 
        new Annotation("Őr","uri1", "Entity"),
        new Annotation("Őrség","uri1", "Entity"),
        new Annotation("Ŋaaa","uri1", "Entity"),
        new Annotation("Orbán Viktor","uri1", "Entity"),
        new Annotation("első","uri1", "Entity")
    };
    
    private static DictionaryStore dictionary;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {        
//        for (int i = 0; i < 4096; i++) {
//            System.out.println(i + " = " + (char)i + "\t\t" + (int)(((char)i) & 0xFFF) + " = " + (char)(((char)i) & 0xFFF));
//        }
//        System.exit(0);
        
        
        dictionary = new DictionaryStore();
        //ReadFile("dictionary.txt");
        
        for (Annotation e : dict3) {
            dictionary.AddOriginalElement(e.getLabel(), e.getUri(), e.getType());
        }
//        DictionaryAnnotator da = new DictionaryAnnotator(dictionary, "None", true, 0, false);
        DictionaryAnnotator da = new DictionaryAnnotator(dictionary, "Hungarian", true, 0, false);

        List<Annotation> entities = da.GetEntities(test3);
        for (Annotation e : entities) {
            System.out.println(e);
        }

    }
    
    public static void ReadFile(String fileName) {

        //reading file line by line in Java using BufferedReader      
        FileInputStream fis = null;
        BufferedReader reader = null;
        int count = 0;
        
        dict2 = new ArrayList<Annotation>();
        
        try {
            fis = new FileInputStream(fileName);
            reader = new BufferedReader(new InputStreamReader(fis));

            String line = reader.readLine();
            while (line != null) {
                dict2.add(new Annotation(line, "uri_" + count++, "Entity"));
                line = reader.readLine();
            }
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        } finally {
            try {
                reader.close();
                fis.close();
            } catch (IOException ex) {
            }
        }
    }
}
