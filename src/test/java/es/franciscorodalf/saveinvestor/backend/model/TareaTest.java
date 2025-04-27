package es.franciscorodalf.saveinvestor.backend.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

import org.junit.jupiter.api.Test;

class TareaTest {

    @Test
    void testConstructorVacio() {
        tarea t = new tarea();
        
        assertNull(t.getId());
        assertNull(t.getConcepto());
        assertEquals(0.0, t.getCantidad());
        assertNull(t.getFecha());
        assertNull(t.getEstado());
        assertNull(t.getUsuarioId());
    }
    
    @Test
    void testConstructorParametrizado() {
        Date fecha = new Date();
        tarea t = new tarea("Pago alquiler", 500.0, fecha, tarea.ESTADO_GASTO, 1);
        
        assertEquals("Pago alquiler", t.getConcepto());
        assertEquals(500.0, t.getCantidad());
        assertEquals(fecha, t.getFecha());
        assertEquals(tarea.ESTADO_GASTO, t.getEstado());
        assertEquals(1, t.getUsuarioId());
    }
    
    @Test
    void testSettersAndGetters() {
        tarea t = new tarea();
        Date fecha = new Date();
        
        t.setId(1);
        t.setConcepto("Depósito");
        t.setCantidad(1000.0);
        t.setFecha(fecha);
        t.setEstado(tarea.ESTADO_INGRESO);
        t.setUsuarioId(2);
        
        assertEquals(1, t.getId());
        assertEquals("Depósito", t.getConcepto());
        assertEquals(1000.0, t.getCantidad());
        assertEquals(fecha, t.getFecha());
        assertEquals(tarea.ESTADO_INGRESO, t.getEstado());
        assertEquals(2, t.getUsuarioId());
    }
    
    @Test
    void testSetCantidadInvalida() {
        tarea t = new tarea();
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            t.setCantidad(0.0);
        });
        
        String expectedMessage = "La cantidad no puede ser cero";
        String actualMessage = exception.getMessage();
        
        assertTrue(actualMessage.contains(expectedMessage));
    }
    
    @Test
    void testSetEstadoInvalido() {
        tarea t = new tarea();
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            t.setEstado("OTRO_ESTADO");
        });
        
        String expectedMessage = "Estado debe ser INGRESO o GASTO";
        String actualMessage = exception.getMessage();
        
        assertTrue(actualMessage.contains(expectedMessage));
    }
    
    @Test
    void testMethodChaining() {
        Date fecha = new Date();
        tarea t = new tarea()
            .concepto("Salario")
            .cantidad(2000.0)
            .fecha(fecha)
            .estado(tarea.ESTADO_INGRESO);
            
        assertEquals("Salario", t.getConcepto());
        assertEquals(2000.0, t.getCantidad());
        assertEquals(fecha, t.getFecha());
        assertEquals(tarea.ESTADO_INGRESO, t.getEstado());
    }
    
    @Test
    void testEquals() {
        tarea t1 = new tarea("Pago", 100.0, new Date(), tarea.ESTADO_GASTO, 1);
        tarea t2 = new tarea("Pago", 200.0, new Date(), tarea.ESTADO_INGRESO, 2);
        tarea t3 = new tarea("Otro concepto", 100.0, new Date(), tarea.ESTADO_GASTO, 1);
        
        // Las tareas se consideran iguales solo por concepto
        assertTrue(t1.equals(t2));
        assertFalse(t1.equals(t3));
    }
    
    @Test
    void testToString() {
        Date fecha = new Date();
        tarea t = new tarea("Descripción", 150.0, fecha, tarea.ESTADO_GASTO, 1);
        
        String expected = "Descripción, 150.0, " + fecha + ", " + tarea.ESTADO_GASTO;
        assertEquals(expected, t.toString());
    }
}
