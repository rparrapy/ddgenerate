package py.gov.datos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Factory de convertidores de archivos.
 */
public class FileConverterFactory {

    private final Logger LOG = LoggerFactory.getLogger(FileConverterFactory.class);

    /**
     * Obtiene el convertidor correspondiente al tipo de conversión que recibe como parámetro.
     *
     * @see py.gov.datos.FileConverterType
     * @param type tipo de conversión que se busca.
     * @return el convertidor correspondiente.
     */
    public FileConverter getFileConverter(FileConverterType type){
        FileConverter converter = null;
        switch (type){
            case XLS_TO_CSV:
                converter = new XlsToCsvConverter();
                break;
            case CSV_TO_HTML:
                converter = new CsvToHtmlConverter();
                break;
            case CSV_TO_OWL:
                converter = new CsvToOwlConverter();
                break;
            case CSV_TO_JSONLD:
                converter = new CsvToJsonLdConverter();
                break;
            case CSV_TO_CONF:
            	converter = new CsvToConfConverter();
            case CSV_TO_DATAPACKAGE:
                converter = new CsvToDataPackageConverter();
        }
        if(converter == null){
            LOG.error("No converter found.");
        }
        return converter;
    }
}
