package py.gov.datos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;

import java.io.*;
import java.util.*;

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
public class CsvToOwlConverter implements FileConverter {

    private final Logger LOG = LoggerFactory.getLogger(CsvToOwlConverter.class);
    private final String SPLIT_BY = ";";
    private List<String> classes = new ArrayList<>();
    private Map<String, String> classProperties = new HashMap<>();
    private Map<String, List<String>> properties = new HashMap<>();


    @Override
    public List<File> convert(List<File> files, String path) {
        TemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode("XML");
        templateResolver.setSuffix(".owl");
        templateResolver.setCacheable(false);

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        File owlDir = new File(path + "owl/");

        if (!owlDir.exists()) {
            owlDir.mkdir();
        }

        List<File> result = new ArrayList<>();
        for (File file : files) {
            if (!file.getName().equals("Clases.csv")) {
                result.add(processClass(file, path, templateEngine));
            }
        }

        return null;
    }

    private File processClass(File file, String path, TemplateEngine templateEngine) {
        FileReader fr = null;
        try {
            fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            String headerLine = br.readLine();
            if(headerLine == null || headerLine.isEmpty()){
                return null;
            }
            String clazz = headerLine.replace("\"", "").replace(";", " ").split("clase")[1];
            this.classes.add(clazz);
            System.out.println(clazz);
            br.readLine();

            while ((line = br.readLine()) != null) {
                List<String> elems = new ArrayList<>(Arrays.asList(line.replace("\"", "").split(SPLIT_BY)));
                if (!elems.isEmpty()) {

                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
