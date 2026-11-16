import { useEffect, useState } from 'react'
import { api } from './api.js'

const PASOS = ['Bolsín', 'Contenido y situación', 'Confirmación', 'Resultado']

export default function App() {
  const [usuarios, setUsuarios] = useState([])
  const [vista, setVista] = useState('inicio') // inicio | listado | detalle | resultado
  const [sesion, setSesion] = useState(null)
  const [bolsines, setBolsines] = useState([])
  const [filtro, setFiltro] = useState({ numeroPrecinto: '', cmOrigen: '' })
  const [detalle, setDetalle] = useState(null)
  const [opcion, setOpcion] = useState(null)
  const [docsMarcadas, setDocsMarcadas] = useState([])
  const [area, setArea] = useState('')
  const [pedido, setPedido] = useState(null) // pedido de confirmación (modal)
  const [resultado, setResultado] = useState(null)
  const [aviso, setAviso] = useState(null) // { tipo: 'error' | 'info' | 'ok', texto }
  const [ocupado, setOcupado] = useState(false)

  useEffect(() => {
    cargarUsuarios()
  }, [])

  async function ejecutar(accion) {
    setOcupado(true)
    setAviso(null)
    try {
      await accion()
    } catch (error) {
      setAviso({ tipo: 'error', texto: error.message })
    } finally {
      setOcupado(false)
    }
  }

  async function cargarUsuarios() {
    try {
      setUsuarios(await api.usuarios())
    } catch (error) {
      setAviso({ tipo: 'error', texto: error.message })
    }
  }

  // Paso 1: el EB selecciona la opción para registrar la recepción de un bolsín
  const iniciar = () =>
    ejecutar(async () => {
      const datos = await api.iniciar()
      setSesion(datos)
      setBolsines(datos.bolsines)
      setFiltro({ numeroPrecinto: '', cmOrigen: '' })
      limpiarSeleccion()
      setVista('listado')
    })

  function limpiarSeleccion() {
    setDetalle(null)
    setOpcion(null)
    setDocsMarcadas([])
    setArea('')
    setPedido(null)
    setResultado(null)
  }

  const filtrar = (evento) => {
    evento?.preventDefault()
    ejecutar(async () => {
      try {
        setBolsines(await api.filtrar(filtro.numeroPrecinto, filtro.cmOrigen))
      } catch (error) {
        setBolsines([])
        throw error
      }
    })
  }

  const limpiarFiltro = () =>
    ejecutar(async () => {
      setFiltro({ numeroPrecinto: '', cmOrigen: '' })
      setBolsines(await api.filtrar('', ''))
    })

  // Paso 5: el EB selecciona un bolsín del listado
  const seleccionarBolsin = (numeroBolsin) =>
    ejecutar(async () => {
      setDetalle(await api.seleccionarBolsin(numeroBolsin))
      setOpcion(null)
      setDocsMarcadas([])
      setArea('')
      setVista('detalle')
    })

  function elegirOpcion(nueva) {
    setOpcion(nueva)
    setAviso(null)
    if (!nueva.requiereSeleccionDocumentacion) setDocsMarcadas([])
    if (!nueva.requiereAreaDestino) setArea('')
  }

  function alternarDoc(numero) {
    setDocsMarcadas((actuales) =>
      actuales.includes(numero) ? actuales.filter((n) => n !== numero) : [...actuales, numero],
    )
  }

  // Paso 8: el EB selecciona la opción de recepción -> el sistema pide confirmación
  const enviarOpcion = () =>
    ejecutar(async () => {
      setPedido(await api.seleccionarOpcion(opcion.codigo, docsMarcadas, area))
    })

  // Paso 10: el EB confirma (o no) la selección
  const responderConfirmacion = (confirma) =>
    ejecutar(async () => {
      const respuesta = await api.confirmar(confirma)
      setPedido(null)
      if (respuesta.registrado) {
        setResultado(respuesta)
        setVista('resultado')
      } else {
        setAviso({ tipo: 'info', texto: respuesta.mensaje })
      }
    })

  const cancelar = () =>
    ejecutar(async () => {
      await api.cancelar()
      limpiarSeleccion()
      setSesion(null)
      setVista('inicio')
      setAviso({ tipo: 'info', texto: 'Se canceló el registro de recepción de bolsín.' })
    })

  const cambiarUsuario = (usuario) =>
    ejecutar(async () => {
      await api.cambiarUsuario(usuario)
      await cargarUsuarios()
      limpiarSeleccion()
      setSesion(null)
      setVista('inicio')
    })

  const reiniciarDatos = () =>
    ejecutar(async () => {
      const respuesta = await api.reiniciarDatos()
      await cargarUsuarios()
      limpiarSeleccion()
      setSesion(null)
      setVista('inicio')
      setAviso({ tipo: 'ok', texto: respuesta.mensaje })
    })

  const usuarioActivo = usuarios.find((u) => u.activo)
  const pasoActual = vista === 'listado' ? 0 : vista === 'detalle' ? (pedido ? 2 : 1) : vista === 'resultado' ? 3 : -1
  const faltaDocumentacion = opcion?.requiereSeleccionDocumentacion && docsMarcadas.length === 0
  const faltaArea = opcion?.requiereAreaDestino && area.trim() === ''

  return (
    <div className="app">
      <header className="barra">
        <div className="marca">
          <span className="logo" aria-hidden="true">B</span>
          <div>
            <strong>Bolsines</strong>
            <small>Gestión de documentación entre comisiones médicas</small>
          </div>
        </div>
        <div className="usuario">
          <div className="usuario-datos">
            <strong>{usuarioActivo?.empleado ?? 'Sin sesión'}</strong>
            <small>{usuarioActivo ? `Encargado de Bolsines · ${usuarioActivo.comisionMedica}` : ''}</small>
          </div>
          <label className="oculto" htmlFor="selector-usuario">Usuario logueado</label>
          <select
            id="selector-usuario"
            value={usuarioActivo?.usuario ?? ''}
            onChange={(e) => cambiarUsuario(e.target.value)}
            disabled={ocupado}
            title="Cambiar de usuario (solo para la demostración)"
          >
            {usuarios.map((u) => (
              <option key={u.usuario} value={u.usuario}>
                {u.usuario} · {u.comisionMedica}
              </option>
            ))}
          </select>
        </div>
      </header>

      <main className="contenido">
        {vista !== 'inicio' && (
          <>
            <nav className="migas" aria-label="Ubicación">
              Inicio <span>›</span> Bolsines <span>›</span> <strong>Registrar recepción de bolsín</strong>
            </nav>
            <ol className="pasos">
              {PASOS.map((nombre, i) => (
                <li key={nombre} className={i < pasoActual ? 'hecho' : i === pasoActual ? 'actual' : ''}>
                  <span className="numero">{i < pasoActual ? '✓' : i + 1}</span>
                  {nombre}
                </li>
              ))}
            </ol>
          </>
        )}

        {aviso && (
          <div className={`aviso ${aviso.tipo}`} role={aviso.tipo === 'error' ? 'alert' : 'status'}>
            <span>{aviso.texto}</span>
            <button className="cerrar" onClick={() => setAviso(null)} aria-label="Cerrar aviso">×</button>
          </div>
        )}

        {vista === 'inicio' && (
          <section className="inicio">
            <h1>Hola{usuarioActivo ? `, ${usuarioActivo.empleado.split(' ')[0]}` : ''}</h1>
            <p className="sub">¿Qué necesitás hacer hoy?</p>
            <div className="tarjetas">
              <button className="tarjeta principal" onClick={iniciar} disabled={ocupado}>
                <span className="icono" aria-hidden="true">📥</span>
                <strong>Registrar recepción de bolsín</strong>
                <span>Controlá el contenido de un bolsín que llegó por correo y registrá su recepción.</span>
              </button>
              <div className="tarjeta deshabilitada" aria-disabled="true">
                <span className="icono" aria-hidden="true">📦</span>
                <strong>Generar bolsín</strong>
                <span>Fuera del alcance de esta entrega.</span>
              </div>
              <div className="tarjeta deshabilitada" aria-disabled="true">
                <span className="icono" aria-hidden="true">🗺️</span>
                <strong>Consultar seguimiento</strong>
                <span>Fuera del alcance de esta entrega.</span>
              </div>
            </div>
            <button className="enlace" onClick={reiniciarDatos} disabled={ocupado}>
              Volver a cargar los datos de prueba
            </button>
          </section>
        )}

        {vista === 'listado' && sesion && (
          <section className="panel">
            <div className="panel-cabecera">
              <div>
                <h1>Bolsines pendientes de recepción</h1>
                <p className="sub">
                  Comisión médica: <strong>{sesion.comisionMedica}</strong>
                </p>
              </div>
            </div>

            {sesion.bolsines.length === 0 ? (
              <div className="vacio">
                <span aria-hidden="true">📭</span>
                <strong>No hay bolsines enviados para {sesion.comisionMedica}</strong>
                <p>Cuando el correo retire un bolsín con destino a tu comisión, va a aparecer acá.</p>
                <button className="boton secundario" onClick={cancelar}>Volver al inicio</button>
              </div>
            ) : (
              <>
                <form className="filtros" onSubmit={filtrar}>
                  <label>
                    N° de precinto
                    <input
                      value={filtro.numeroPrecinto}
                      onChange={(e) => setFiltro({ ...filtro, numeroPrecinto: e.target.value })}
                      placeholder="Ej.: 100234"
                      inputMode="numeric"
                    />
                  </label>
                  <label>
                    CM de origen
                    <input
                      value={filtro.cmOrigen}
                      onChange={(e) => setFiltro({ ...filtro, cmOrigen: e.target.value })}
                      placeholder="Ej.: Rosario"
                    />
                  </label>
                  <button className="boton" type="submit" disabled={ocupado}>Filtrar</button>
                  <button className="boton secundario" type="button" onClick={limpiarFiltro} disabled={ocupado}>
                    Limpiar
                  </button>
                </form>

                {bolsines.length > 0 && (
                  <table className="tabla">
                    <thead>
                      <tr>
                        <th>N° de bolsín</th>
                        <th>CM de origen</th>
                        <th>N° de precinto</th>
                        <th></th>
                      </tr>
                    </thead>
                    <tbody>
                      {bolsines.map((b) => (
                        <tr key={b.numeroBolsin}>
                          <td>#{b.numeroBolsin}</td>
                          <td>{b.cmOrigen}</td>
                          <td className="mono">{b.numeroPrecinto}</td>
                          <td className="derecha">
                            <button
                              className="boton chico"
                              onClick={() => seleccionarBolsin(b.numeroBolsin)}
                              disabled={ocupado}
                            >
                              Seleccionar
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
                <div className="acciones">
                  <button className="boton fantasma" onClick={cancelar} disabled={ocupado}>Cancelar</button>
                  <span className="contador">
                    {bolsines.length} de {sesion.bolsines.length} bolsines
                  </span>
                </div>
              </>
            )}
          </section>
        )}

        {vista === 'detalle' && detalle && (
          <section className="dos-columnas">
            <div className="panel">
              <div className="panel-cabecera">
                <div>
                  <h1>Bolsín #{detalle.numeroBolsin}</h1>
                  <p className="sub">
                    Origen: <strong>{detalle.cmOrigen}</strong> · Precinto{' '}
                    <strong className="mono">{detalle.numeroPrecinto}</strong>
                  </p>
                </div>
              </div>
              <p className="ayuda">
                Compará el contenido físico del bolsín con lo registrado.
                {opcion?.requiereSeleccionDocumentacion && ' Marcá la documentación que presenta la novedad.'}
              </p>
              {detalle.remitos.map((remito) => (
                <div className="remito" key={remito.numeroRemito}>
                  <h2>Remito N° {remito.numeroRemito}</h2>
                  <ul>
                    {remito.documentacion.map((doc) => {
                      const marcada = docsMarcadas.includes(doc.numero)
                      return (
                        <li key={doc.numero} className={marcada ? 'marcada' : ''}>
                          <label>
                            {opcion?.requiereSeleccionDocumentacion && (
                              <input type="checkbox" checked={marcada} onChange={() => alternarDoc(doc.numero)} />
                            )}
                            <span className="doc-texto">
                              <strong>{doc.asunto}</strong>
                              <small>Doc. N° {doc.numero}</small>
                            </span>
                            <span className="etiqueta">{doc.tipoDocumento}</span>
                          </label>
                        </li>
                      )
                    })}
                  </ul>
                </div>
              ))}
            </div>

            <div className="panel lateral">
              <h2>¿Cómo llegó el bolsín?</h2>
              <div className="opciones" role="radiogroup" aria-label="Opciones de recepción">
                {detalle.opciones.map((o) => (
                  <label
                    key={o.codigo}
                    className={`opcion ${opcion?.codigo === o.codigo ? 'elegida' : ''} ${o.habilitada ? '' : 'inactiva'}`}
                  >
                    <input
                      type="radio"
                      name="opcion"
                      checked={opcion?.codigo === o.codigo}
                      disabled={!o.habilitada}
                      onChange={() => elegirOpcion(o)}
                    />
                    <span>
                      {o.descripcion}
                      {!o.habilitada && <small>Solo disponible en la Comisión Médica Central</small>}
                    </span>
                  </label>
                ))}
              </div>

              {opcion?.requiereAreaDestino && (
                <label className="campo">
                  Área de destino en la sede central
                  <input value={area} onChange={(e) => setArea(e.target.value)} placeholder="Ej.: Asuntos Legales" />
                </label>
              )}
              {opcion?.requiereSeleccionDocumentacion && (
                <p className="ayuda">
                  {docsMarcadas.length === 0
                    ? 'Marcá en el listado la documentación correspondiente.'
                    : `${docsMarcadas.length} documentación(es) marcada(s).`}
                </p>
              )}

              <div className="acciones columna">
                <button
                  className="boton"
                  onClick={enviarOpcion}
                  disabled={ocupado || !opcion || faltaDocumentacion || faltaArea}
                >
                  Registrar recepción
                </button>
                <button className="boton secundario" onClick={iniciar} disabled={ocupado}>
                  Elegir otro bolsín
                </button>
                <button className="boton fantasma" onClick={cancelar} disabled={ocupado}>Cancelar</button>
              </div>
            </div>
          </section>
        )}

        {vista === 'resultado' && resultado && (
          <section className="panel">
            <div className="exito">
              <span className="tilde" aria-hidden="true">✓</span>
              <div>
                <h1>{resultado.mensaje}</h1>
                <p className="sub">
                  Estado del bolsín: <strong>Recibido en CM destino</strong> · Responsable:{' '}
                  <strong>{resultado.responsable}</strong> · {resultado.fechaHora}
                </p>
              </div>
            </div>

            <h2>Situación de remitos y documentación</h2>
            {resultado.remitos.map((remito) => (
              <div className="remito" key={remito.numeroRemito}>
                <h2>
                  Remito N° {remito.numeroRemito}
                  <span className={`estado ${claseEstado(remito.estado)}`}>{textoEstado(remito.estado)}</span>
                </h2>
                <ul>
                  {remito.documentacion.map((doc) => (
                    <li key={doc.numero}>
                      <label>
                        <span className="doc-texto">
                          <strong>{doc.asunto}</strong>
                          <small>
                            Doc. N° {doc.numero} · {doc.tipoDocumento}
                            {doc.areaDestino ? ` · Área: ${doc.areaDestino}` : ''}
                          </small>
                        </span>
                        <span className={`estado ${claseEstado(doc.estado)}`}>{doc.estado}</span>
                      </label>
                    </li>
                  ))}
                </ul>
              </div>
            ))}

            <h2>Notificaciones enviadas</h2>
            {resultado.notificaciones.map((n) => (
              <details className="correo" key={n.destinatario}>
                <summary>
                  <span aria-hidden="true">✉️</span> {n.destinatario}
                </summary>
                <p><strong>{n.asunto}</strong></p>
                <pre>{n.cuerpo}</pre>
              </details>
            ))}

            <div className="acciones">
              <button className="boton" onClick={iniciar} disabled={ocupado}>Registrar otra recepción</button>
              <button
                className="boton secundario"
                onClick={() => {
                  limpiarSeleccion()
                  setVista('inicio')
                }}
              >
                Volver al inicio
              </button>
            </div>
          </section>
        )}
      </main>

      {pedido && (
        <div className="fondo-modal">
          <div className="modal" role="dialog" aria-modal="true" aria-labelledby="titulo-modal">
            <h2 id="titulo-modal">{pedido.mensaje}</h2>
            <dl>
              <dt>Situación</dt>
              <dd>{pedido.descripcion}</dd>
              {pedido.numerosDocumentacion.length > 0 && (
                <>
                  <dt>Documentación con novedad</dt>
                  <dd>{pedido.numerosDocumentacion.map((n) => `N° ${n}`).join(', ')}</dd>
                </>
              )}
              {pedido.areaDestino && (
                <>
                  <dt>Área de destino</dt>
                  <dd>{pedido.areaDestino}</dd>
                </>
              )}
            </dl>
            <p className="ayuda">Al confirmar se notifica por e-mail a la comisión médica de origen.</p>
            <div className="acciones">
              <button className="boton secundario" onClick={() => responderConfirmacion(false)} disabled={ocupado}>
                No confirmar
              </button>
              <button className="boton" onClick={() => responderConfirmacion(true)} disabled={ocupado} autoFocus>
                Confirmar recepción
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

function claseEstado(estado) {
  const texto = (estado ?? '').toLowerCase()
  if (texto.includes('rechazad') || texto.includes('no recibid')) return 'malo'
  if (texto.includes('parcial') || texto.includes('redirigir')) return 'medio'
  return 'bueno'
}

function textoEstado(nombre) {
  // "RecibidoYAceptadoParcial" -> "Recibido y aceptado parcial"
  return (nombre ?? '')
    .replace(/([a-zA-Z])(?=[A-Z])/g, '$1 ')
    .toLowerCase()
    .replace(/^./, (c) => c.toUpperCase())
}
