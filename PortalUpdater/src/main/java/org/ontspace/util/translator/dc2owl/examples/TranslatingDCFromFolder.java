/*
ont-space - The ontology-based resource metadata repository
Copyright (c) 2006-2011, Information Eng. Research Unit - Univ. of Alcalá
http://www.cc.uah.es/ie
This library is free software; you can redistribute it and/or modify it under
the terms of the GNU Lesser General Public License as published by the Free
Software Foundation; either version 2.1 of the License, or (at your option)
any later version.
This library is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
details.
You should have received a copy of the GNU Lesser General Public License along
with this library; if not, write to the Free Software Foundation, Inc.,
59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.ontspace.util.translator.dc2owl.examples;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ontspace.MetadataRecordReference;
import org.ontspace.MetadataRepository;
import org.ontspace.dc.owl.DCQueryManagerImpl;
import org.ontspace.dc.translator.DublinCore;
import org.ontspace.owl.MetadataRepositoryFactory;
import org.ontspace.owl.util.MetadataRepositoryConfigException;
import org.ontspace.owl.util.OntologyNotInRepositoryException;
import org.ontspace.owl.util.OntspaceConfiguration;
import org.ontspace.owl.util.QMConfiguration;

/**
 * This class contains the code to create a new ont-space repository and
 * to translate and store some content items that are present in XML in a local
 * folder
 */
public class TranslatingDCFromFolder {

    /**
     * The main method is the starting point of the application
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        //SECTION 1: Configuration
        String contentDir;
        contentDir = System.getProperty("user.dir")
            + System.getProperty("file.separator") + "etc"
            + System.getProperty("file.separator") + "metadata";
        String configFileOntSpace = System.getProperty("user.dir")
            + System.getProperty("file.separator") + "etc"
            + System.getProperty("file.separator") + "create.xml";
        System.out.println("Preconfigured paramenters: ");
        System.out.println(" - contentDir: " + contentDir);
        System.out.println(" - Ont-space configuration file: "
            + configFileOntSpace);

        //SECTION 2: Repository and QueryManagers creation
        MetadataRepository rep = null;
        DCQueryManagerImpl qm = null;

        try {
            OntspaceConfiguration confOntSpace = new OntspaceConfiguration(
                configFileOntSpace);

            try {
                rep = MetadataRepositoryFactory.createOrOpenMetadataRepository(
                    confOntSpace);
            } catch (MetadataRepositoryConfigException ex1) {
                Logger.getLogger(TranslatingDCFromFolder.class.getName()).
                    log(Level.SEVERE, null, ex1);
            }

            HashMap<String, Object> params =
                new HashMap<String, Object>(confOntSpace.getOntologies());
            QMConfiguration qmConf = confOntSpace.getQms().get(
                QMConfiguration.QMType.DC);
            Iterator<String> qmSpecificOntsIt = qmConf.getOntologyUris().
                iterator();
            String specificOnt;
            HashMap<String, Object> qmParams = new HashMap<String, Object>();
            while (qmSpecificOntsIt.hasNext()) {
                specificOnt = qmSpecificOntsIt.next();
                qmParams.put(specificOnt, params.get(specificOnt));
            }
            System.out.println("QM java class: " + qmConf.getJavaClass());
            qm = (DCQueryManagerImpl) rep.getQueryManager(qmConf.getJavaClass(),
                qmParams);

        } catch (MetadataRepositoryConfigException ex) {
            Logger.getLogger(TranslatingDCFromFolder.class.getName()).
                log(Level.SEVERE, null, ex);
        } catch (OntologyNotInRepositoryException ex) {
            Logger.getLogger(TranslatingDCFromFolder.class.getName()).log(
                Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(TranslatingDCFromFolder.class.getName()).log(
                Level.SEVERE, null, ex);
        }

        System.out.println("Public repository URI: " + rep.getRepositoryURI());

        //SECTION 3: Reading the XML files with the metadata
        FileFilter xmlFilter = new FileFilter() {

            @Override
            public boolean accept(File file) {
                return (file.isFile() && file.getName().endsWith(".xml"));
            }
        };
        File dir = new File(contentDir);
        List<File> listXmlFiles = new ArrayList();
        if (dir.isDirectory()) {
            if (!dir.exists()) {
                System.out.println("Error: The Directory not exists");
            }
            File[] files = dir.listFiles(xmlFilter);
            listXmlFiles.addAll(Arrays.asList(files));
        }

        //SECTION 4: Storing the content items in the ont-space repository
        for (int i = 0; i < listXmlFiles.size(); i++) {
            String filePath = listXmlFiles.get(i).getAbsolutePath();

            System.out.println("Parseando el fichero " + filePath);

            try {
                DublinCore dc = new DublinCore(listXmlFiles.get(i));
                dc.parseDCXML();
                MetadataRecordReference storeNewDublinCore = qm.
                    storeNewDublinCore(dc);
                System.out.println("LO id: " + storeNewDublinCore.
                    getLocalMetadataRecordId());
            } catch (Exception ex) {
                Logger.getLogger(TranslatingDCFromFolder.class.getName()).log(
                    Level.SEVERE, null, ex);
            }
        }

        //SECTION 5: Closing the ont-space repository
        rep.close();
    }
}
