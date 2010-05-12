/**
 * MessageResourceUtilTest.java
 *
 * Copyright 2010-2010 Daniel de Aguiar Kamakura
 */
package com.kamakura.datalogger.i18n;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.kamakura.communication.i18n.MessageResourceUtil;

/**
 * @author Daniel de Aguiar Kamakura
 *
 * @since 1.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/spring/*.xml"})
public class MessageResourceUtilTest {
  @Test
  public void testGetMessage() {
    MessageResourceUtil.setLocale(new Locale("pt", "BR"));
    
    assertEquals("Erro ao abrir streams.", MessageResourceUtil.getMessage("error.opening.streams"));
    assertEquals("Sinal de Término de Dados não encontrado.", MessageResourceUtil.getMessage("error.end.data.signal.not.found"));
  }
}
