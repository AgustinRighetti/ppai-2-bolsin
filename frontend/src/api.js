// Acceso a la API del backend. Cada función corresponde a un mensaje EB -> Pantalla del CU 28.

async function llamar(ruta, cuerpo, metodo = 'POST') {
  let respuesta
  try {
    respuesta = await fetch(`/api${ruta}`, {
      method: metodo,
      headers: { 'Content-Type': 'application/json' },
      body: metodo === 'GET' ? undefined : JSON.stringify(cuerpo ?? {}),
    })
  } catch {
    throw new Error('No se pudo conectar con el servidor. Verifique que el backend esté en ejecución.')
  }
  const datos = await respuesta.json().catch(() => null)
  if (!respuesta.ok) {
    throw new Error(datos?.mensaje ?? datos?.message ?? 'Ocurrió un error inesperado.')
  }
  return datos
}

export const api = {
  iniciar: () => llamar('/recepcion-bolsin/iniciar'),
  filtrar: (numeroPrecinto, cmOrigen) => llamar('/recepcion-bolsin/filtrar', { numeroPrecinto, cmOrigen }),
  seleccionarBolsin: (numeroBolsin) => llamar('/recepcion-bolsin/seleccionar-bolsin', { numeroBolsin }),
  seleccionarOpcion: (opcion, numerosDocumentacion, areaDestino) =>
    llamar('/recepcion-bolsin/seleccionar-opcion', { opcion, numerosDocumentacion, areaDestino }),
  confirmar: (confirma) => llamar('/recepcion-bolsin/confirmar', { confirma }),
  cancelar: () => llamar('/recepcion-bolsin/cancelar'),
  usuarios: () => llamar('/demo/usuarios', null, 'GET'),
  cambiarUsuario: (usuario) => llamar('/demo/cambiar-usuario', { usuario }),
  reiniciarDatos: () => llamar('/demo/reiniciar-datos'),
}
