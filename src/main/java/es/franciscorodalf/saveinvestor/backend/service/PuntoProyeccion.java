package es.franciscorodalf.saveinvestor.backend.service;

/**
 * Representa un punto dentro de la proyección mensual generada por el
 * simulador de metas.
 */
public record PuntoProyeccion(int periodo, double saldo, double aporteAcumulado, double interesAcumulado) {
}
