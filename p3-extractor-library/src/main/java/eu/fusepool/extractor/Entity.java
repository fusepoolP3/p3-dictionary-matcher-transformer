/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.fusepool.extractor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.activation.MimeType;


public interface Entity {

    MimeType getType();

    InputStream getData() throws IOException;
    
    void writeData(OutputStream out) throws IOException;
    
}
