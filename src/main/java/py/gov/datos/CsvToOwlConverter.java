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

/**
 * Convertidor de CSV a OWL.
 */
public class CsvToOwlConverter implements FileConverter {

    private final Logger LOG = LoggerFactory.getLogger(CsvToOwlConverter.class);
    private final String SPLIT_BY = ";";
    protected Map<String, OwlClass> classes = new HashMap<>();
    protected Map<String, OwlProperty> properties = new HashMap<>();



    @Override
    public List<File> convert(List<File> files, String path, Map<String, String> params) {
        TemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode("LEGACYHTML5");
        templateResolver.setSuffix(".owl");
        templateResolver.setCacheable(false);

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.setTemplateResolver(templateResolver);

        File owlDir = new File(path + "def/");
        owlDir.mkdir();
        List<File> result = new ArrayList<>();
        for (File file : files) {
            if (file.getName().equals("Clases.csv")) {
                processIndex(file);
            }else{
                processClass(file);
            }
        }

        IContext context = new Context();

        context.getVariables().put("classes", classes.values());

        List<OwlProperty> objectProperties = new ArrayList<>();
        List<OwlProperty> datatypeProperties = new ArrayList<>();

        for(OwlProperty p: properties.values()){
            if(p.isObjectProperty()){
                objectProperties.add(p);
            }else{
                datatypeProperties.add(p);
            }
        }

        context.getVariables().put("datatypeProperties", datatypeProperties);
        context.getVariables().put("objectProperties", objectProperties);
        String dncp = templateEngine.process("dncp", context);

        try {
            result.add(writeToFile("dncp", path, dncp));
        } catch (IOException e) {
            LOG.error("Can not create output file");
        }

        return result;
    }

    /**
     * Obtiene las variables de contexto relacionadas a las clases OWL,
     * a partir del archivo .csv correspondiente.
     *
     * @param file archivo Clases.csv
     */
    protected void processIndex(File file) {
        FileReader fr = null;
        try {
            fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;

            ArrayList<String> columns = new ArrayList<>();
            boolean flag = false;
            while ((line = br.readLine()) != null) {
                List<String> elems = new ArrayList<>(Arrays.asList(line.replace("\"", "").split(SPLIT_BY)));
                if(!elems.isEmpty() && !line.replace(" ", "").isEmpty()){
                    if(flag){
                        while (elems.size() < columns.size()) {
                            elems.add("");
                        }
                        OwlClass clazz = parseOwlClass(elems);
                        classes.put(clazz.getNombre(), clazz);
                    }
                    if(elems.get(0).equals("Clases")){
                        flag = true;
                        columns = new ArrayList<>(elems);
                    }

                }
            }
            fr.close();
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Obtiene las variables de contexto relacionadas a las propiedades de cada clase
     * OWL, a partir del archivo .csv correspondiente.
     *
     * @param file archivo CSV correspondiente a una clase en particular.
     */
    protected void processClass(File file) {
        FileReader fr = null;
        try {
            fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            String headerLine = br.readLine();
            if(headerLine == null || headerLine.isEmpty()){
                fr.close();
                br.close();
                return;
            }
            String clazzName = headerLine.replace("\"", "").replace(";", "").split("clase")[1].trim();
            OwlClass clazz = classes.get(clazzName);
            ArrayList<String> columns = new ArrayList<>(Arrays.asList(br.readLine().replace("\"", "").split(SPLIT_BY)));

            while ((line = br.readLine()) != null) {
                List<String> elems = new ArrayList<>(Arrays.asList(line.replace("\"", "").split(SPLIT_BY)));
                if (!elems.isEmpty() && elems.get(0).length() > 0) {
                    while (elems.size() < columns.size()) {
                        elems.add("");
                    }
                    OwlProperty prop = parseOwlProperty(elems, clazz);
                    if(!properties.containsKey(prop.getOwlName())){
                        properties.put(prop.getOwlName(), prop);
                    }
                    prop.addClass(clazz);
                    clazz.addProperty(prop);
                    String cardElem = elems.get(8);
                    if(cardElem.equals("1") || cardElem.toLowerCase().equals("single")){
                        OwlCardinality card = new OwlCardinality();
                        card.setCardinalidad(1);
                        card.setPropiedad(prop);
                        clazz.addCardinalidad(card);
                    }
                }
            }
            fr.close();
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Parsea una propiedad OWL a partir de la lista de elementos de una fila del CSV correspondiente.
     * @param elems fila de elementos CSV.
     * @param clazz clase a la cual corresponde la propiedad.
     * @return instancia de OwlProperty correspondiente a la fila.
     */
    private OwlProperty parseOwlProperty(List<String> elems, OwlClass clazz) {
        String nombre = elems.get(2).trim().replaceAll(" +", " ");
        String nombreJSON = elems.get(0).trim().replaceAll(" +", " ");
        OwlProperty prop = new OwlProperty(nombre); //Debería ser 1 pero no está completo.
        prop.setNombreJSON(nombreJSON);
        if(prop.getNombre().equals("identifier")){
            prop.setNombre("identifier" + clazz.getOwlName());
        }
        prop.setDescripcionEspanhol(elems.get(11));
        prop.setDescripcionIngles(elems.get(12));
        String tipo = elems.get(7);

        if(tipo.startsWith("xsd")){
            prop.setTipo(tipo);
        }else{
            prop.setTipo(new OwlClass(tipo).getOwlName());
        }

        List<String> labelsEspanhol = new ArrayList<>(Arrays.asList(elems.get(4).split(",")));
        labelsEspanhol.add(elems.get(2));
        removeEmpty(labelsEspanhol);
        prop.setLabelsEspanhol(labelsEspanhol);

        List<String> labelsIngles = new ArrayList<>(Arrays.asList(elems.get(5).split(",")));
        prop.setLabelsIngles(labelsIngles);
        removeEmpty(labelsIngles);
        return prop;
    }

    /**
     * Elimina los espacios vacíos de una lista de cadenas.
     * Si un elemento está compuesto solo por espacios, se elimina.
     *
     * @param elems lista de elementos a procesar.
     */
    private void removeEmpty(List<String> elems){
        Iterator<String> i = elems.iterator();
        while(i.hasNext()){
            String l = i.next();
            if(l.replace(" ", "").isEmpty()){
                i.remove();
            }
        }
    }

    /**
     * Parsea una clase OWL a partir de la lista de elementos de una fila del CSV correspondiente.
     *
     * @param elems fila de elementos CSV.
     * @return instancia de OwlClass correspondiente.
     */
    private OwlClass parseOwlClass(List<String> elems){
        OwlClass clazz = new OwlClass(elems.get(0));
        clazz.setLabelEspanhol(elems.get(1));
        clazz.setLabelIngles(elems.get(2));
        clazz.setDescripcionEspanhol(elems.get(3));
        clazz.setDescripcionIngles(elems.get(4));

        return clazz;
    }


    /**
     * Crea un archivo de salida y escribe el contenido correspondiente.
     *
     * @param name el nombre del archivo a escribir.
     * @param path la ruta donde se encuentra el archivo.
     * @param content el contenido a escribir en el archivo.
     * @return el archivo creado.
     * @throws IOException
     */
    private File writeToFile(String name, String path, String content) throws IOException {
        File outputFile = new File(path + "def/" + name + ".owl");
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

}

