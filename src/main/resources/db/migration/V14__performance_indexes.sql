-- Optimización de índices para mejorar performance de queries frecuentes

-- Índices para búsquedas de propiedades
CREATE INDEX IF NOT EXISTS idx_propiedad_ciudad_visible ON propiedad(ciudad, visible) WHERE visible = true;
CREATE INDEX IF NOT EXISTS idx_propiedad_tipo_visible ON propiedad(tipo_propiedad, visible) WHERE visible = true;
CREATE INDEX IF NOT EXISTS idx_propiedad_precio_noche ON propiedad(precio_noche);
CREATE INDEX IF NOT EXISTS idx_propiedad_puntuacion ON propiedad(puntuacion_promedio) WHERE puntuacion_promedio > 0;

-- Índices para reservas (queries frecuentes)
CREATE INDEX IF NOT EXISTS idx_reserva_viajero_estado ON reserva(viajero_id, estado);
CREATE INDEX IF NOT EXISTS idx_reserva_propiedad_estado ON reserva(propiedad_id, estado);
CREATE INDEX IF NOT EXISTS idx_reserva_fechas ON reserva(fecha_checkin, fecha_checkout);
CREATE INDEX IF NOT EXISTS idx_reserva_codigo ON reserva(codigo_reserva);

-- Índices para pagos
CREATE INDEX IF NOT EXISTS idx_pago_reserva ON pago(reserva_id);
CREATE INDEX IF NOT EXISTS idx_pago_estado_fecha ON pago(estado, created_date);
CREATE INDEX IF NOT EXISTS idx_pago_stripe ON pago(stripe_payment_intent_id);

-- Índices para mensajes (chat)
CREATE INDEX IF NOT EXISTS idx_mensaje_conversacion ON mensaje(remitente_id, destinatario_id, created_date);
CREATE INDEX IF NOT EXISTS idx_mensaje_leido ON mensaje(destinatario_id, leido) WHERE leido = false;

-- Índices para valoraciones
CREATE INDEX IF NOT EXISTS idx_valoracion_propiedad_visible ON valoracion(propiedad_id, visible) WHERE visible = true;
CREATE INDEX IF NOT EXISTS idx_valoracion_valorado ON valoracion(valorado_id);
CREATE INDEX IF NOT EXISTS idx_valoracion_puntuacion ON valoracion(puntuacion);

-- Índices para notificaciones
CREATE INDEX IF NOT EXISTS idx_notificacion_usuario_leida ON notificaciones(usuario_id, leida, created_date);
CREATE INDEX IF NOT EXISTS idx_notificacion_tipo_fecha ON notificaciones(tipo, created_date);

-- Índices compuestos para queries complejas de búsqueda
CREATE INDEX IF NOT EXISTS idx_propiedad_busqueda_completa 
ON propiedad(ciudad, tipo_propiedad, precio_noche, visible) 
WHERE visible = true;

-- Índices para mejora de performance en joins
CREATE INDEX IF NOT EXISTS idx_propiedad_anfitrion_visible ON propiedad(anfitrion_id, visible);
CREATE INDEX IF NOT EXISTS idx_usuario_rol_activo ON usuario(rol, activo) WHERE activo = true;

-- Índices GIN para búsqueda de texto (si se usa full-text search en futuro)
-- CREATE INDEX IF NOT EXISTS idx_propiedad_titulo_gin ON propiedad USING gin(to_tsvector('spanish', titulo));
-- CREATE INDEX IF NOT EXISTS idx_propiedad_descripcion_gin ON propiedad USING gin(to_tsvector('spanish', descripcion));

-- Análisis de estadísticas para el optimizador de queries
ANALYZE propiedad;
ANALYZE reserva;
ANALYZE pago;
ANALYZE valoracion;
ANALYZE mensaje;
ANALYZE notificaciones;
ANALYZE usuario;
