package com.ecommerce.service;

import com.ecommerce.dto.DireccionDTO;
import com.ecommerce.dto.DireccionRequestDTO;
import com.ecommerce.model.Direccion;
import com.ecommerce.model.Usuario;
import com.ecommerce.repository.DireccionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DireccionService {

    @Autowired
    private DireccionRepository direccionRepository;

    @Autowired
    private UsuarioService usuarioService;

    @SuppressWarnings("null")
    @Transactional
    public DireccionDTO crearDireccion(Usuario targetUser, DireccionRequestDTO request) {
        Direccion direccion = new Direccion();
        direccion.setUsuario(targetUser);
        updateDireccionFields(direccion, request);

        // Control principal/preferida
        long cuentaDirecciones = direccionRepository.countByUsuario(targetUser);
        if (cuentaDirecciones == 0) {
            direccion.setPreferida(true);
        } else if (Boolean.TRUE.equals(request.getPreferida())) {
            unsetOtherPreferred(targetUser);
            direccion.setPreferida(true);
        }

        return DireccionDTO.fromEntity(direccionRepository.save(direccion));
    }

    @SuppressWarnings("null")
    @Transactional
    public DireccionDTO actualizarDireccion(Long id, DireccionRequestDTO request, Usuario accionUsuario) {
        Direccion direccion = direccionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dirección no encontrada"));

        validarAccesos(direccion, accionUsuario, "EDITAR");

        updateDireccionFields(direccion, request);

        if (Boolean.TRUE.equals(request.getPreferida()) && !direccion.getPreferida()) {
            unsetOtherPreferred(direccion.getUsuario());
            direccion.setPreferida(true);
        } else if (Boolean.FALSE.equals(request.getPreferida()) && direccion.getPreferida()) {
            direccion.setPreferida(false);
        }

        return DireccionDTO.fromEntity(direccionRepository.save(direccion));
    }

    public List<DireccionDTO> listarMisDirecciones(Usuario usuario) {
        return direccionRepository.findByUsuario(usuario).stream()
                .map(DireccionDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una dirección por ID. ADMIN puede ver cualquiera;
     * CUSTOMER y SUPPLIER solo las propias.
     */
    @SuppressWarnings("null")
    public DireccionDTO obtenerDireccion(Long id, Usuario accionUsuario) {
        Direccion direccion = direccionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dirección no encontrada con id: " + id));
        validarAccesos(direccion, accionUsuario, "LEER");
        return DireccionDTO.fromEntity(direccion);
    }

    public List<DireccionDTO> listarDireccionesPorUsuarioId(Long usuarioId, Usuario accionUsuario) {
        if (!accionUsuario.getRole().name().equals("ADMIN") && !accionUsuario.getId().equals(usuarioId)) {
            throw new RuntimeException("Acceso Denegado: No puedes ver direcciones de otros usuarios");
        }
        Usuario targetUser = usuarioService.buscarPorId(usuarioId);
        return direccionRepository.findByUsuario(targetUser).stream()
                .map(DireccionDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<DireccionDTO> listarTodasLasDirecciones(Usuario accionUsuario) {
        if (!"ADMIN".equals(accionUsuario.getRole().name())) {
            throw new RuntimeException("Solo ADMIN puede ver todas las direcciones");
        }
        return direccionRepository.findAll().stream().map(DireccionDTO::fromEntity).collect(Collectors.toList());
    }

    @SuppressWarnings("null")
    @Transactional
    public void eliminarDireccion(Long id, Usuario accionUsuario) {
        Direccion direccion = direccionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dirección no encontrada"));

        validarAccesos(direccion, accionUsuario, "ELIMINAR");

        if ("SUPPLIER".equals(accionUsuario.getRole().name())) {
            throw new RuntimeException("Proveedores no tienen permiso para eliminar direcciones");
        }

        direccionRepository.delete(direccion);
    }

    private void updateDireccionFields(Direccion direccion, DireccionRequestDTO request) {
        direccion.setAlias(request.getAlias());
        direccion.setCalle(request.getCalle());
        direccion.setNumeroExterior(request.getNumeroExterior());
        direccion.setNumeroInterior(request.getNumeroInterior());
        direccion.setReferencia(request.getReferencia());
        direccion.setColonia(request.getColonia());
        direccion.setCiudad(request.getCiudad());
        direccion.setMunicipio(request.getMunicipio());
        direccion.setEstado(request.getEstado());
        direccion.setCodigoPostal(request.getCodigoPostal());
        direccion.setTelefonos(request.getTelefonos());
    }

    private void unsetOtherPreferred(Usuario usuario) {
        List<Direccion> direcciones = direccionRepository.findByUsuario(usuario);
        for (Direccion d : direcciones) {
            if (d.getPreferida()) {
                d.setPreferida(false);
                direccionRepository.save(d);
            }
        }
    }

    private void validarAccesos(Direccion d, Usuario accionUsuario, String accion) {
        if ("ADMIN".equals(accionUsuario.getRole().name())) {
            return;
        }
        if (!d.getUsuario().getId().equals(accionUsuario.getId())) {
            throw new RuntimeException("Acceso Denegado a esta dirección");
        }
    }
}
