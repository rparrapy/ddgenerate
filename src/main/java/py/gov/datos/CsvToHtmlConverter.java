package py.gov.datos;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/*
 * @author	Rodrigo Parra	
 * @copyright	2014 Governance and Democracy Program USAID-CEAMSO
 * @license 	http://www.gnu.org/licenses/gpl-2.0.html
 * 
 * USAID-CEAMSO
 * Copyright (C) 2014 Governance and Democracy Program
 * http://ceamso.org.py/es/proyectos/20-programa-de-democracia-y-gobernabilidad
 * 
----------------------------------------------------------------------------
 * This file is part of the Governance and Democracy Program USAID-CEAMSO,
 * is distributed as free software in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. You can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License version 2 as published by the 
 * Free Software Foundation, accessible from <http://www.gnu.org/licenses/> or write 
 * to Free Software Foundation (FSF) Inc., 51 Franklin St, Fifth Floor, Boston, 
 * MA 02111-1301, USA.
 ---------------------------------------------------------------------------
 * Este archivo es parte del Programa de Democracia y Gobernabilidad USAID-CEAMSO,
 * es distribuido como software libre con la esperanza que sea de utilidad,
 * pero sin NINGUNA GARANTÍA; sin garantía alguna implícita de ADECUACION a cualquier
 * MERCADO o APLICACION EN PARTICULAR. Usted puede redistribuirlo y/o modificarlo 
 * bajo los términos de la GNU Lesser General Public Licence versión 2 de la Free 
 * Software Foundation, accesible en <http://www.gnu.org/licenses/> o escriba a la 
 * Free Software Foundation (FSF) Inc., 51 Franklin St, Fifth Floor, Boston, 
 * MA 02111-1301, USA.
 */
public class CsvToHtmlConverter implements FileConverter {
    private final Logger LOG = LoggerFactory.getLogger(XlsToCsvConverter.class);
    private final String SPLIT_BY = ";";


    @Override
    public List<File> convert(List<File> files, String path) {
        TemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode("LEGACYHTML5");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheable(false);

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        File htmlDir = new File(path + "html/");

        if (!htmlDir.exists()) {
            htmlDir.mkdir();
        }

        copyResources(path);

        for (File file : files) {
            if (file.getName().equals("Clases.csv")) {
                makeIndex(file, path, templateEngine);
            } else {
                makePage(file, path, templateEngine);
            }
        }
        return null;
    }


    private File makeIndex(File file, String path, TemplateEngine templateEngine) {
        FileReader fr = null;
        IContext context = new Context();
        try {
            fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            String reference = br.readLine().replace("\"", "").replace(";", " ");
            String tableTitle = br.readLine().replace("\"", "").replace(";", "");
            context.getVariables().put("title", tableTitle);
            context.getVariables().put("reference", reference);

            boolean st = false;
            List<String> headerOne = new ArrayList<>();
            List<List<String>> tableOne = new ArrayList<>();
            List<String> headerTwo = new ArrayList<>();
            List<List<String>> tableTwo = new ArrayList<>();
            List<String> urls = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                List<String> elems = new ArrayList<>(Arrays.asList(line.replace("\"", "").split(SPLIT_BY)));
                if (!elems.isEmpty()) {
                    if (elems.size() >= 2 && elems.get(1).equals("es")) {
                        headerOne.addAll(elems);
                        //System.out.println(headerOne);
                        continue;
                    }

                    if (elems.get(0).equals("Clases")) {
                        headerTwo.addAll(elems);
                        st = true;
                        //System.out.println(headerTwo);
                        continue;
                    }
                    if (st) {
                        if (elems.size() > 1) {
                            while (elems.size() < headerTwo.size()) {
                                elems.add("");
                            }
                            tableTwo.add(elems);
                            String name = elems.get(0);
                            name = name.toLowerCase().replace("á", "a").replace("é", "e").replace("í", "i")
                                    .replace("ó", "o").replace("ú", "u").replace(" ", "_");
                            urls.add(name + ".html");
                        }
                    } else {
                        if (elems.size() > 1) {
                            while (elems.size() < headerOne.size()) {
                                elems.add("");
                            }
                            tableOne.add(elems);
                        }
                    }
                }
            }
            //System.out.println(tableOne);
            //System.out.println(tableTwo);
            context.getVariables().put("headerOne", headerOne);
            context.getVariables().put("headerTwo", headerTwo);
            context.getVariables().put("tableOne", tableOne);
            context.getVariables().put("tableTwo", tableTwo);
            context.getVariables().put("urls", urls);

            String result = templateEngine.process("index", context);
            //System.out.println(result);
            writeToFile("index", path, result);

            br.close();
            fr.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private File makePage(File file, String path, TemplateEngine templateEngine) {
        FileReader fr = null;
        IContext context = new Context();
        try {
            fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            String headerLine = br.readLine();
            if(headerLine == null || headerLine.isEmpty()){
                return null;
            }
            String tableTitle = headerLine.replace("\"", "").replace(";", "");
            context.getVariables().put("title", tableTitle);

            List<String> header = new ArrayList<>();
            List<List<String>> table = new ArrayList<>();
            int cont = 0;

            while ((line = br.readLine()) != null) {
                List<String> elems = new ArrayList<>(Arrays.asList(line.replace("\"", "").split(SPLIT_BY)));
                if (!elems.isEmpty()) {
                if(cont == 0){
                    header.addAll(elems);
                }else{
                    while (elems.size() < header.size()) {
                        elems.add("");
                    }
                    table.add(elems);
                }
                    cont++;
                }
            }
            context.getVariables().put("header", header);
            context.getVariables().put("table", table);

            //System.out.println(header);
            //System.out.println(table);

            String result = templateEngine.process("page", context);
            //System.out.println(result);
            writeToFile(file.getName().substring(0, file.getName().indexOf('.')), path, result);

            br.close();
            fr.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private File writeToFile(String name, String path, String content) throws IOException {
        name = name.toLowerCase().replace("á", "a").replace("é", "e").replace("í", "i")
                .replace("ó", "o").replace("ú", "u").replace(" ", "_");
        File outputFile = new File(path + "html/" + name + ".html");
        if (outputFile.createNewFile()) {
            Writer out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outputFile), "UTF-8"));
            out.write(content);
            out.flush();
            out.close();
        } else {
            LOG.error("Can not create output file");
        }
        return outputFile;

    }

    private void copyResources(String path) {
        byte[] buffer = new byte[1024];
        InputStream is = getClass().getResourceAsStream("/html_resources.zip");
        ZipInputStream zis = new ZipInputStream(is);
        ZipEntry ze;

        try {
            while ((ze = zis.getNextEntry()) != null) {
                String fileName = ze.getName();
                File newFile = new File(path + "html/" + fileName);
                if (ze.isDirectory()) {
                    newFile.mkdir();
                } else {
                    FileOutputStream fos = new FileOutputStream(newFile);

                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
            }

            zis.closeEntry();
            zis.close();
        } catch (IOException e) {
            LOG.error("Can not extract zip file content");
            e.printStackTrace();
        }
    }
}
