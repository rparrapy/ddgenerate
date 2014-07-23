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
public class OwlClass {

    private String nombre;
    private List<OwlCardinality> cardinalidades;
    private String labelEspanhol;
    private String labelIngles;
    private String descripcionEspanhol;
    private String descripcionIngles;

    public OwlClass(String nombre) {
        this.nombre = nombre;
        this.cardinalidades = new ArrayList<>();
    }


    public List<OwlCardinality> getCardinalidades() {
        return cardinalidades;
    }

    public void addCardinalidad(OwlCardinality cardinalidad) {
        this.cardinalidades.add(cardinalidad);
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getLabelEspanhol() {
        return labelEspanhol;
    }

    public void setLabelEspanhol(String labelEspanhol) {
        this.labelEspanhol = labelEspanhol;
    }

    public String getLabelIngles() {
        return labelIngles;
    }

    public void setLabelIngles(String labelIngles) {
        this.labelIngles = labelIngles;
    }

    public String getDescripcionIngles() {
        return descripcionIngles;
    }

    public void setDescripcionIngles(String descripcionIngles) {
        this.descripcionIngles = descripcionIngles;
    }

    public String getDescripcionEspanhol() {
        return descripcionEspanhol;
    }

    public void setDescripcionEspanhol(String descripcionEspanhol) {
        this.descripcionEspanhol = descripcionEspanhol;
    }
}
