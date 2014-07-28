package py.gov.datos.model;

import java.util.ArrayList;
import java.util.List;

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
public class OwlProperty {
    private String nombre;
    private List<String> labelsEspanhol;
    private List<String> labelsIngles;
    private String tipo;
    private String descripcionEspanhol;
    private String descripcionIngles;
    private List<OwlClass> classes;

    public OwlProperty(String nombre) {
        this.nombre = nombre;
        this.classes = new ArrayList<>();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<String> getLabelsEspanhol() {
        return labelsEspanhol;
    }

    public void addLabelEspanhol(String labelEspanhol) {
        this.labelsEspanhol.add(labelEspanhol);
    }

    public List<String> getLabelsIngles() {
        return labelsIngles;
    }

    public void addLabelIngles(String labelIngles) {
        this.labelsIngles.add(labelIngles);
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcionEspanhol() {
        return descripcionEspanhol;
    }

    public void setDescripcionEspanhol(String descripcionEspanhol) {
        this.descripcionEspanhol = descripcionEspanhol;
    }

    public String getDescripcionIngles() {
        return descripcionIngles;
    }

    public void setDescripcionIngles(String descripcionIngles) {
        this.descripcionIngles = descripcionIngles;
    }

    public void setLabelsEspanhol(List<String> labelsEspanhol) {
        this.labelsEspanhol = labelsEspanhol;
    }

    public void setLabelsIngles(List<String> labelsIngles) {
        this.labelsIngles = labelsIngles;
    }

    public List<OwlClass> getClasses() {
        return classes;
    }

    public void setClasses(List<OwlClass> classes) {
        this.classes = classes;
    }

    public void addClass(OwlClass clazz){
        if(clazz != null)
            this.classes.add(clazz);
    }

    public String getOwlName(){
        String result = this.nombre.replace("á", "a").replace("é", "e").replace("í", "i")
                .replace("ó", "o").replace("ú", "u").replace(" ", "_");
        int prefixIndex = result.indexOf("_");
        if(prefixIndex > 0){
            String prefix = result.substring(0, prefixIndex);
            String suffix = result.substring(result.indexOf("_") + 1, result.length());
            return prefix.toLowerCase() + toCamelCase(suffix);
        }
        return this.nombre.toLowerCase();
    }

    private String toCamelCase(String s){
        String[] parts = s.split("_");
        String camelCaseString = "";
        for (String part : parts){
            camelCaseString = camelCaseString + toProperCase(part);
        }
        return camelCaseString;
    }

    private String toProperCase(String s) {
        return s.substring(0, 1).toUpperCase() +
                s.substring(1).toLowerCase();
    }

    public boolean isObjectProperty(){
        if(this.tipo != null && !this.tipo.startsWith("xsd")){
            return true;
        }
        return false;
    }

    public String getBasicType(){
        return this.tipo.split(":")[1];
    }
}
