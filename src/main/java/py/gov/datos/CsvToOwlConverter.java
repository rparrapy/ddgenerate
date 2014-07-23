package py.gov.datos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.TemplateResolver;
import py.gov.datos.model.OwlCardinality;
import py.gov.datos.model.OwlClass;
import py.gov.datos.model.OwlProperty;

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
    private Map<String, OwlClass> classes = new HashMap<>();
    private Map<String, OwlProperty> properties = new HashMap<>();



    @Override
    public List<File> convert(List<File> files, String path, Map<String, String> params) {
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
            if (file.getName().equals("Clases.csv")) {
                processIndex(file, path, templateEngine);
            }else{
                processClass(file, path, templateEngine);
            }
        }

        IContext context = new Context();
        context.getVariables().put("classes", classes.values());
        context.getVariables().put("properties", properties.values());
        String dncp = templateEngine.process("dncp", context);
        System.out.println(dncp);

        return null;
    }

    private void processIndex(File file, String s, TemplateEngine templateEngine) {
        FileReader fr = null;
        try {
            fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;

            ArrayList<String> columns = new ArrayList<>();
            boolean flag = false;
            while ((line = br.readLine()) != null) {
                List<String> elems = new ArrayList<>(Arrays.asList(line.replace("\"", "").split(SPLIT_BY)));
                if(!elems.isEmpty()){
                    if(flag){
                        while (elems.size() < columns.size()) {
                            elems.add("");
                        }
                        OwlClass clazz = new OwlClass(elems.get(0));
                        clazz.setLabelEspanhol(elems.get(1));
                        clazz.setLabelIngles(elems.get(2));
                        clazz.setDescripcionEspanhol(elems.get(3));
                        clazz.setDescripcionIngles(elems.get(4));
                        classes.put(clazz.getNombre(), clazz);
                    }
                    if(elems.get(0).equals("Clases")){
                        flag = true;
                        columns = new ArrayList<>(elems);
                    }

                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processClass(File file, String path, TemplateEngine templateEngine) {
        FileReader fr = null;
        try {
            fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            String headerLine = br.readLine();
            if(headerLine == null || headerLine.isEmpty()){
                return;
            }
            String clazzName = headerLine.replace("\"", "").replace(";", "").split("clase")[1].trim();
            OwlClass clazz = classes.get(clazzName);
            ArrayList<String> columns = new ArrayList<>(Arrays.asList(br.readLine().replace("\"", "").split(SPLIT_BY)));

            while ((line = br.readLine()) != null) {
                List<String> elems = new ArrayList<>(Arrays.asList(line.replace("\"", "").split(SPLIT_BY)));
                if (!elems.isEmpty()) {
                    while (elems.size() < columns.size()) {
                        elems.add("");
                    }
                    OwlProperty prop = parseOwlProperty(elems);
                    String cardElem = elems.get(8);
                    if(cardElem.equals("1") || cardElem.toLowerCase().equals("single")){
                        OwlCardinality card = new OwlCardinality();
                        card.setCardinalidad(1);
                        card.setPropiedad(prop);
                        //clazz.addCardinalidad(card);
                    }

                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return;
    }

    private OwlProperty parseOwlProperty(List<String> elems) {
        OwlProperty prop = new OwlProperty(elems.get(3)); //Debería ser 1 pero no está completo.
        prop.setDescripcionEspanhol(elems.get(11));
        prop.setDescripcionIngles(elems.get(12));
        prop.setTipo(elems.get(7));
        List<String> labelsEspanhol = new ArrayList<>(Arrays.asList(elems.get(4).split(",")));
        labelsEspanhol.add(elems.get(2));
        prop.setLabelsEspanhol(labelsEspanhol);

        List<String> labelsIngles = new ArrayList<>(Arrays.asList(elems.get(5).split(",")));
        prop.setLabelsIngles(labelsIngles);
        return prop;
    }


}
