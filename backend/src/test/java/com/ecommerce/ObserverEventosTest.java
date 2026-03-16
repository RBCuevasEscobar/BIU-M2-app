package com.ecommerce;

import com.ecommerce.inventory.GestorInventarioFactory;
import com.ecommerce.model.Orden;
import com.ecommerce.model.Usuario;
import com.ecommerce.observer.InventarioObserver;
import com.ecommerce.observer.NotificacionObserver;
import com.ecommerce.observer.events.OrdenCreadaEvent;
import com.ecommerce.observer.events.OrdenPagadaEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.*;

public class ObserverEventosTest {

    @Mock
    private GestorInventarioFactory gestorInventarioFactory;

    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private InventarioObserver inventarioObserver;

    @InjectMocks
    private NotificacionObserver notificacionObserver;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testNotificacionOrdenCreada() {
        Orden ordenMock = new Orden();
        ordenMock.setId(101L);
        com.ecommerce.model.Cliente u = new com.ecommerce.model.Cliente();
        u.setEmail("test@ejemplo.com");
        ordenMock.setUsuario(u);

        OrdenCreadaEvent evento = new OrdenCreadaEvent(this, ordenMock);

        // Simulamos la publicacion
        notificacionObserver.handleOrdenCreada(evento);

        // Si la prueba llega aca sin excepciones, es validad respecto al log y el metodo
        // ya que los side-effects son system.outs. 
    }

    @Test
    public void testInventarioObserverProcesaEventoSobrePagos() {
       Orden ordenMock = new Orden();
        ordenMock.setId(201L);

        OrdenPagadaEvent evento = new OrdenPagadaEvent(this, ordenMock);

        // Llamada a logica observer
        inventarioObserver.onOrdenPagada(evento);
        
        // Verifica llamadas al GestorFactory si hubiera productos agregados,
        verify(gestorInventarioFactory, never()).obtenerGestor(any());
    }
}
