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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Convertidor de XLSX a CSV.
 */
public class XlsToCsvConverter implements FileConverter{

    private final  Logger LOG = LoggerFactory.getLogger(XlsToCsvConverter.class);

    @Override
    public List<File> convert(List<File> files, String path, Map<String, String> params) {
        List<File> result = new ArrayList<File>();
        for(File file: files){
            result.addAll(convert(file, path));
        }
        return result;
    }

    /**
     * Convierte un único archivo XLSX a uno o más archivos CSV.
     *
     * @param file el archivo a convertir.
     * @param path la ruta del directorio de salida.
     * @return los archivos generados.
     */
    private List<File> convert(File file, String path) {
        List<File> result = new ArrayList<File>();
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(file));
            File csvDir = new File(path + "csv/");
            csvDir.mkdir();
            for(int i= 0; i < workbook.getNumberOfSheets(); i++){
                XSSFSheet sheet = workbook.getSheetAt(i);
                File outputFile = new File(path + "csv/" + sheet.getSheetName() + ".csv");
                if(outputFile.createNewFile()){
                    FileOutputStream out = new FileOutputStream(outputFile);
                    StringBuffer content = this.convertSheet(sheet);
                    //System.out.println(content);
                    out.write(content.toString().getBytes());
                    result.add(outputFile);
                    out.flush();
                    out.close();
                }else{
                    LOG.error("Can not create output file");
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Convierte una hoja de una planilla XLSX a un archivo .csv
     * @param sheet hoja a convertir.
     * @return un archivo .csv
     */
    private StringBuffer convertSheet(XSSFSheet sheet){
        StringBuffer data = new StringBuffer();
        Iterator<Row> rowIterator = sheet.iterator();

        while(rowIterator.hasNext()){
            Row row = rowIterator.next();
            StringBuffer rowBuffer = convertRow(row);
            data.append(rowBuffer);
        }

        return data;
    }

    /**
     * Convierte una fila de una hoja XLSX a un archivo .csv
     *
     * @param row fila a convertir.
     * @return fila del archivo .csv.
     */
    private StringBuffer convertRow(Row row){
        StringBuffer data = new StringBuffer();
        Iterator<Cell> cellIterator = row.cellIterator();

        while (cellIterator.hasNext()){
            StringBuffer cellBuffer = convertCell(cellIterator.next());
            data.append(cellBuffer);
        }
        if(data.length() > 0){
            data = data.deleteCharAt(data.length() - 1);
        }
        data.append("\r\n");
        return data;
    }

    /**
     * Convierte una celda de una planilla XLSX a un elemento de un archivo .csv.
     * @param cell celda a convertir.
     * @return elemento generado.
     */
    private StringBuffer convertCell(Cell cell){
        StringBuffer data = new StringBuffer();

        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_BOOLEAN:
                data.append(cell.getBooleanCellValue() + ";");

                break;
            case Cell.CELL_TYPE_NUMERIC:
                data.append(new DataFormatter().formatCellValue(cell) + ";");

                break;
            case Cell.CELL_TYPE_STRING:
                data.append(CustomStringEscapeUtils.escapeCsv(cell.getStringCellValue()) + ";");
                break;

            case Cell.CELL_TYPE_BLANK:
                data.append("" + ";");
                break;
            default:
                data.append(CustomStringEscapeUtils.escapeCsv(cell.getStringCellValue()) + ";");
        }

        return data;
    }
}
