/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.fraunhofer.igd.klarschiff.web;

import de.fraunhofer.igd.klarschiff.vo.Vorgang;
import java.io.Serializable;

/**
 *
 * @author rvoss
 */
public class Command implements Serializable {
  
  Vorgang vorgang;
  
  public Vorgang getVorgang() {
    return vorgang;
  }

  public void setVorgang(Vorgang vorgang) {
    this.vorgang = vorgang;
  }
}
