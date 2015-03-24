package py.gov.datos;

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

import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.OrderedJSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import py.gov.datos.model.OwlClass;
import py.gov.datos.model.OwlCardinality;
import py.gov.datos.model.OwlProperty;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;

/**
 * Convertidor de CSV a context JSON-LD
 */
public class CsvToJsonLdConverter extends CsvToOwlConverter {
    private final Logger LOG = LoggerFactory.getLogger(CsvToJsonLdConverter.class);

    @Override
    public List<File> convert(List<File> files, String path, Map<String, String> params) {
        File contextsDir = new File(path + "contexts/");
        contextsDir.mkdir();
        for (File file : files) {
            if (file.getName().equals("Clases.csv")) {
                processIndex(file);
            } else {
                processClass(file);
            }
        }

        File contextDir = new File(path + "contexts/");
        contextDir.mkdir();

        List<File> result = null;
        try {
            result = parseContent(path);
        } catch (JSONException e) {
            LOG.error("Can not parse JSON.");
        }

        return result;
    }

    /**
     * Convierte a JSON las clases y propiedades parseadas del CSV.
     * @param path ruta de salida
     * @return
     * @throws JSONException
     */
    private List<File> parseContent(String path) throws JSONException {
        List<File> result = new ArrayList<>();

        for (OwlClass clazz : this.classes.values()) {
            OrderedJSONObject content = new OrderedJSONObject();
            content.put("xsd", "http://www.w3.org/2001/XMLSchema#");
            content.put("dncp", "http://www.contrataciones.gov.py/datos/def/dncp.owl");
            content.put("@language", "es-419");

            if (clazz.getNombre().equals("Llamado")) {
                content.put("dct", "http://purl.org/dc/terms/");
                content.put("dcat", "http://www.w3.org/ns/dcat#");
                content.put("dataset", "dcat:Distribution");
                content.put("titulo", "dct:title");
                content.put("descripcion", "dct:description");
                OrderedJSONObject licencia = new OrderedJSONObject();
                licencia.put("@id", "dct:license");
                licencia.put("@type", "@id");
                content.put("licencia", licencia);
                OrderedJSONObject formato = new OrderedJSONObject();
                formato.put("@id", "dct:license");
                formato.put("@type", "@id");
                content.put("formato", formato);
            }

            OrderedJSONObject clazzName = new OrderedJSONObject();
            clazzName.put("@id", "dncp:" + clazz.getOwlName());
            content.put(clazz.getOwlName().toLowerCase(), clazzName);

            for (OwlProperty property : clazz.getProperties()) {
                OrderedJSONObject elem = new OrderedJSONObject();
                elem.put("@id", "dncp:" + property.getOwlName());
                if (property.isObjectProperty()) {
                    elem.put("@type", "@id");
                } else {
                    elem.put("@type", "xsd:" + property.getBasicType());
                }
                content.put(property.getNombreJSON(), elem);
            }

            try {
            	String str = content.toString(4);
            	str = str.replace("\\/", "/");
                result.add(writeToFile(clazz.getNombre().toLowerCase(), path, str));
            } catch (IOException e) {
                LOG.error("Can not create output file");
            }
        }
        return result;
    }

    /**
     * Crea un archivo de salida y escribe el contenido correspondiente.
     *
     * @param name    el nombre del archivo a escribir.
     * @param path    la ruta donde se encuentra el archivo.
     * @param content el contenido a escribir en el archivo.
     * @return el archivo creado.
     * @throws java.io.IOException
     */
    private File writeToFile(String name, String path, String content) throws IOException {
        String formattedName = Normalizer
                .normalize(name, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "").replaceAll(" +", "_");

        File outputFile = new File(path + "contexts/" + formattedName + ".json");
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
