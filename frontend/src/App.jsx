import { useEffect, useState } from 'react'
import { api } from './api.js'

const PASOS = ['Bolsín', 'Contenido y situación', 'Confirmación', 'Resultado']
const ESTADO_PREVIO_DOCUMENTACION = 'En bolsín enviado'

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

  const volverAlInicio = () => {
    limpiarSeleccion()
    setVista('inicio')
  }

  const usuarioActivo = usuarios.find((u) => u.activo)
  const pasoActual = vista === 'listado' ? 0 : vista === 'detalle' ? (pedido ? 2 : 1) : vista === 'resultado' ? 3 : -1
  const faltaDocumentacion = opcion?.requiereSeleccionDocumentacion && docsMarcadas.length === 0
  const faltaArea = opcion?.requiereAreaDestino && area.trim() === ''
  const totalDocumentacion = detalle?.remitos.reduce((total, r) => total + r.documentacion.length, 0) ?? 0

  return (
    <div className="app">
      {/* ---------- Riel de navegación ---------- */}
      <aside className="riel">
        <div className="marca">
          <span className="sello" aria-hidden="true">B</span>
          <div>
            <strong>Bolsines</strong>
            <small>Comisiones médicas</small>
          </div>
        </div>

        <nav className="menu" aria-label="Módulos">
          <span className="menu-titulo">Operaciones</span>
          <button className={vista === 'inicio' ? 'activo' : ''} onClick={volverAlInicio} disabled={ocupado}>
            <Icono nombre="inicio" /> Inicio
          </button>
          <button className={vista !== 'inicio' ? 'activo' : ''} onClick={iniciar} disabled={ocupado}>
            <Icono nombre="recibir" /> Registrar recepción
          </button>
          <span className="deshabilitado" title="Fuera del alcance de esta entrega">
            <Icono nombre="enviar" /> Generar bolsín
          </span>
          <span className="deshabilitado" title="Fuera del alcance de esta entrega">
            <Icono nombre="mapa" /> Seguimiento
          </span>
        </nav>

        <div className="demo">
          <span className="menu-titulo">Demostración</span>
          <label htmlFor="selector-usuario">Usuario logueado</label>
          <select
            id="selector-usuario"
            value={usuarioActivo?.usuario ?? ''}
            onChange={(e) => cambiarUsuario(e.target.value)}
            disabled={ocupado}
            title="Simula el inicio de sesión (solo para la demostración)"
          >
            {usuarios.map((u) => (
              <option key={u.usuario} value={u.usuario}>
                {u.usuario} · {u.comisionMedica}
              </option>
            ))}
          </select>
          <button className="enlace" onClick={reiniciarDatos} disabled={ocupado}>
            Volver a cargar los datos de prueba
          </button>
        </div>
      </aside>

      {/* ---------- Área de trabajo ---------- */}
      <div className="marco">
        <header className="cabecera">
          <nav className="migas" aria-label="Ubicación">
            <span>Inicio</span>
            {vista !== 'inicio' && (
              <>
                <span className="separador">/</span>
                <span>Bolsines</span>
                <span className="separador">/</span>
                <strong>Registrar recepción de bolsín</strong>
              </>
            )}
          </nav>
          <div className="usuario">
            <span className="avatar" aria-hidden="true">{iniciales(usuarioActivo?.empleado)}</span>
            <div>
              <strong>{usuarioActivo?.empleado ?? 'Sin sesión'}</strong>
              <small>{usuarioActivo ? `Encargado de Bolsines · ${usuarioActivo.comisionMedica}` : ''}</small>
            </div>
          </div>
        </header>

        <main className="contenido">
          {vista !== 'inicio' && <Pasos actual={pasoActual} />}

          {aviso && (
            <div className={`aviso ${aviso.tipo}`} role={aviso.tipo === 'error' ? 'alert' : 'status'}>
              <span>{aviso.texto}</span>
              <button className="cerrar" onClick={() => setAviso(null)} aria-label="Cerrar aviso">
                ×
              </button>
            </div>
          )}

          {/* ---------- Inicio ---------- */}
          {vista === 'inicio' && (
            <section className="inicio">
              <p className="sobretitulo">{fechaDeHoy()}</p>
              <h1>Hola{usuarioActivo ? `, ${usuarioActivo.empleado.split(' ')[0]}` : ''}.</h1>
              <p className="sub">¿Llegó un bolsín? Controlá su contenido y registrá la recepción.</p>

              <button className="accion-principal" onClick={iniciar} disabled={ocupado}>
                <span className="accion-icono" aria-hidden="true">
                  <Icono nombre="recibir" />
                </span>
                <span className="accion-texto">
                  <strong>Registrar recepción de bolsín</strong>
                  <span>Seleccioná un bolsín enviado a tu comisión, compará el contenido y confirmá.</span>
                </span>
                <span className="flecha" aria-hidden="true">→</span>
              </button>

              <div className="otras">
                <div className="otra">
                  <Icono nombre="enviar" />
                  <div>
                    <strong>Generar bolsín</strong>
                    <span>Fuera del alcance de esta entrega</span>
                  </div>
                </div>
                <div className="otra">
                  <Icono nombre="mapa" />
                  <div>
                    <strong>Consultar seguimiento</strong>
                    <span>Fuera del alcance de esta entrega</span>
                  </div>
                </div>
              </div>
            </section>
          )}

          {/* ---------- Paso 1: listado de bolsines ---------- */}
          {vista === 'listado' && sesion && (
            <section className="hoja">
              <div className="hoja-cabecera">
                <div>
                  <p className="sobretitulo">Paso 1 de 4</p>
                  <h1>Bolsines pendientes de recepción</h1>
                  <p className="sub">
                    Enviados a <strong>{sesion.comisionMedica}</strong>
                  </p>
                </div>
                {sesion.bolsines.length > 0 && (
                  <div className="cifra">
                    <strong>{sesion.bolsines.length}</strong>
                    <span>en tránsito</span>
                  </div>
                )}
              </div>

              {sesion.bolsines.length === 0 ? (
                <div className="vacio">
                  <Icono nombre="vacio" />
                  <strong>No hay bolsines enviados para {sesion.comisionMedica}</strong>
                  <p>Cuando el correo retire un bolsín con destino a tu comisión, va a aparecer acá.</p>
                  <button className="boton secundario" onClick={cancelar}>
                    Volver al inicio
                  </button>
                </div>
              ) : (
                <>
                  <form className="filtros" onSubmit={filtrar}>
                    <label>
                      N° de precinto
                      <input
                        value={filtro.numeroPrecinto}
                        onChange={(e) => setFiltro({ ...filtro, numeroPrecinto: e.target.value })}
                        placeholder="100234"
                        inputMode="numeric"
                        className="mono"
                      />
                    </label>
                    <label>
                      CM de origen
                      <input
                        value={filtro.cmOrigen}
                        onChange={(e) => setFiltro({ ...filtro, cmOrigen: e.target.value })}
                        placeholder="Rosario"
                      />
                    </label>
                    <div className="filtros-botones">
                      <button className="boton" type="submit" disabled={ocupado}>
                        Filtrar
                      </button>
                      <button className="boton fantasma" type="button" onClick={limpiarFiltro} disabled={ocupado}>
                        Limpiar
                      </button>
                    </div>
                  </form>

                  {bolsines.length > 0 && (
                    <ul className="bolsines">
                      {bolsines.map((b) => (
                        <li key={b.numeroBolsin}>
                          <button onClick={() => seleccionarBolsin(b.numeroBolsin)} disabled={ocupado}>
                            <span className="bolsin-numero">
                              <small>Bolsín</small>#{b.numeroBolsin}
                            </span>
                            <span className="bolsin-origen">
                              <small>Comisión médica de origen</small>
                              {b.cmOrigen}
                            </span>
                            <Precinto numero={b.numeroPrecinto} />
                            <span className="bolsin-accion">Seleccionar →</span>
                          </button>
                        </li>
                      ))}
                    </ul>
                  )}

                  <div className="pie-acciones">
                    <button className="boton fantasma" onClick={cancelar} disabled={ocupado}>
                      Cancelar
                    </button>
                    <span className="contador">
                      Mostrando {bolsines.length} de {sesion.bolsines.length}
                    </span>
                  </div>
                </>
              )}
            </section>
          )}

          {/* ---------- Paso 2: contenido y situación ---------- */}
          {vista === 'detalle' && detalle && (
            <section className="dos-columnas">
              <div className="hoja">
                <div className="hoja-cabecera">
                  <div>
                    <p className="sobretitulo">Paso 2 de 4</p>
                    <h1>Bolsín #{detalle.numeroBolsin}</h1>
                    <p className="sub">
                      Desde <strong>{detalle.cmOrigen}</strong>
                    </p>
                  </div>
                  <Precinto numero={detalle.numeroPrecinto} grande />
                </div>

                <dl className="resumen">
                  <div>
                    <dt>Remitos</dt>
                    <dd>{detalle.remitos.length}</dd>
                  </div>
                  <div>
                    <dt>Documentación</dt>
                    <dd>{totalDocumentacion}</dd>
                  </div>
                  <div>
                    <dt>Con novedad</dt>
                    <dd className={docsMarcadas.length ? 'resaltado' : ''}>{docsMarcadas.length}</dd>
                  </div>
                </dl>

                <p className="ayuda">
                  Compará el contenido físico del bolsín con lo registrado.
                  {opcion?.requiereSeleccionDocumentacion && <strong> Marcá la documentación que presenta la novedad.</strong>}
                </p>

                {detalle.remitos.map((remito) => (
                  <div className="remito" key={remito.numeroRemito}>
                    <div className="remito-cabecera">
                      <span>Remito</span>
                      <strong className="mono">N° {remito.numeroRemito}</strong>
                      <span className="remito-cantidad">{remito.documentacion.length} doc.</span>
                    </div>
                    <ul>
                      {remito.documentacion.map((doc) => {
                        const marcada = docsMarcadas.includes(doc.numero)
                        const seleccionable = opcion?.requiereSeleccionDocumentacion
                        return (
                          <li key={doc.numero} className={`${marcada ? 'marcada' : ''} ${seleccionable ? 'seleccionable' : ''}`}>
                            <label>
                              {seleccionable && (
                                <input type="checkbox" checked={marcada} onChange={() => alternarDoc(doc.numero)} />
                              )}
                              <span className="doc-texto">
                                <strong>{doc.asunto}</strong>
                                <small className="mono">Doc. N° {doc.numero}</small>
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

              <aside className="hoja lateral">
                <h2>¿Cómo llegó el bolsín?</h2>
                <p className="ayuda">Elegí la situación que corresponde a la recepción.</p>
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
                      <span className="opcion-numero" aria-hidden="true">{o.codigo}</span>
                      <span className="opcion-texto">
                        {o.descripcion}
                        {!o.habilitada && <small>Solo disponible en la Comisión Médica Central</small>}
                      </span>
                    </label>
                  ))}
                </div>

                {opcion?.requiereAreaDestino && (
                  <label className="campo">
                    Área de destino en la sede central
                    <input value={area} onChange={(e) => setArea(e.target.value)} placeholder="Asuntos Legales" />
                  </label>
                )}
                {opcion?.requiereSeleccionDocumentacion && (
                  <p className={`indicador ${docsMarcadas.length ? 'listo' : ''}`}>
                    {docsMarcadas.length === 0
                      ? 'Marcá en el listado la documentación correspondiente.'
                      : `${docsMarcadas.length} documentación(es) marcada(s).`}
                  </p>
                )}

                <div className="acciones-columna">
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
                  <button className="boton fantasma" onClick={cancelar} disabled={ocupado}>
                    Cancelar
                  </button>
                </div>
              </aside>
            </section>
          )}

          {/* ---------- Paso 4: resultado ---------- */}
          {vista === 'resultado' && resultado && (
            <section className="hoja">
              <div className="exito">
                <span className="tilde" aria-hidden="true">✓</span>
                <div>
                  <h1>{resultado.mensaje}</h1>
                  <p className="sub">
                    Bolsín en estado <strong>Recibido en CM destino</strong> · Responsable{' '}
                    <strong>{resultado.responsable}</strong> · <span className="mono">{resultado.fechaHora}</span>
                  </p>
                </div>
              </div>

              <h2 className="titulo-seccion">Cambios de estado registrados</h2>
              {resultado.remitos.map((remito) => (
                <div className="remito" key={remito.numeroRemito}>
                  <div className="remito-cabecera">
                    <span>Remito</span>
                    <strong className="mono">N° {remito.numeroRemito}</strong>
                    <span className={`estado ${claseEstado(remito.estado)}`}>{textoEstado(remito.estado)}</span>
                  </div>
                  <ul>
                    {remito.documentacion.map((doc) => (
                      <li key={doc.numero}>
                        <div className="fila-resultado">
                          <span className="doc-texto">
                            <strong>{doc.asunto}</strong>
                            <small>
                              <span className="mono">Doc. N° {doc.numero}</span> · {doc.tipoDocumento}
                              {doc.areaDestino ? ` · Área: ${doc.areaDestino}` : ''}
                            </small>
                          </span>
                          <span className="transicion">
                            <span className="estado previo">{ESTADO_PREVIO_DOCUMENTACION}</span>
                            <span className="flechita" aria-label="pasa a">→</span>
                            <span className={`estado ${claseEstado(doc.estado)}`}>{doc.estado}</span>
                          </span>
                        </div>
                      </li>
                    ))}
                  </ul>
                </div>
              ))}

              <h2 className="titulo-seccion">Notificaciones enviadas</h2>
              {resultado.notificaciones.map((n) => (
                <details className="correo" key={n.destinatario}>
                  <summary>
                    <Icono nombre="correo" /> {n.destinatario}
                  </summary>
                  <p>
                    <strong>{n.asunto}</strong>
                  </p>
                  <pre>{n.cuerpo}</pre>
                </details>
              ))}

              <div className="pie-acciones">
                <button className="boton" onClick={iniciar} disabled={ocupado}>
                  Registrar otra recepción
                </button>
                <button className="boton secundario" onClick={volverAlInicio}>
                  Volver al inicio
                </button>
              </div>
            </section>
          )}
        </main>
      </div>

      {/* ---------- Paso 3: pedido de confirmación ---------- */}
      {pedido && (
        <div className="fondo-modal">
          <div className="modal" role="dialog" aria-modal="true" aria-labelledby="titulo-modal">
            <p className="sobretitulo">Paso 3 de 4 · Confirmación</p>
            <h2 id="titulo-modal">{pedido.mensaje}</h2>
            <dl>
              <dt>Situación</dt>
              <dd>{pedido.descripcion}</dd>
              {pedido.numerosDocumentacion.length > 0 && (
                <>
                  <dt>Documentación con novedad</dt>
                  <dd className="mono">{pedido.numerosDocumentacion.map((n) => `N° ${n}`).join(', ')}</dd>
                </>
              )}
              {pedido.areaDestino && (
                <>
                  <dt>Área de destino</dt>
                  <dd>{pedido.areaDestino}</dd>
                </>
              )}
            </dl>
            <p className="ayuda">Al confirmar se notifica por e-mail a la comisión médica de origen. No se puede deshacer.</p>
            <div className="modal-acciones">
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

function Pasos({ actual }) {
  return (
    <ol className="pasos" aria-label="Pasos del registro">
      {PASOS.map((nombre, i) => (
        <li
          key={nombre}
          className={i < actual ? 'hecho' : i === actual ? 'actual' : ''}
          aria-current={i === actual ? 'step' : undefined}
        >
          <span className="numero">{i < actual ? '✓' : i + 1}</span>
          <span className="nombre">{nombre}</span>
        </li>
      ))}
    </ol>
  )
}

function Precinto({ numero, grande = false }) {
  return (
    <span className={`precinto ${grande ? 'grande' : ''}`}>
      <small>Precinto</small>
      <strong className="mono">{numero}</strong>
    </span>
  )
}

function Icono({ nombre }) {
  const trazos = {
    inicio: <path d="M3 10.5 12 3l9 7.5V21h-6v-6H9v6H3z" />,
    recibir: (
      <>
        <path d="M4 13h4l2 3h4l2-3h4" />
        <path d="M4 13 6.5 5h11L20 13v6H4z" />
        <path d="M12 3v7m-3-3 3 3 3-3" />
      </>
    ),
    enviar: (
      <>
        <path d="M3 7h18v13H3z" />
        <path d="m3 7 9 6 9-6" />
      </>
    ),
    mapa: (
      <>
        <path d="M9 4 3 6v14l6-2 6 2 6-2V4l-6 2z" />
        <path d="M9 4v14m6-12v14" />
      </>
    ),
    correo: (
      <>
        <path d="M3 6h18v12H3z" />
        <path d="m3 6 9 7 9-7" />
      </>
    ),
    vacio: (
      <>
        <path d="M4 13h4l2 3h4l2-3h4" />
        <path d="M4 13 6.5 5h11L20 13v6H4z" />
      </>
    ),
  }
  return (
    <svg className="icono" viewBox="0 0 24 24" aria-hidden="true" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
      {trazos[nombre]}
    </svg>
  )
}

function iniciales(nombre) {
  if (!nombre) return '?'
  return nombre
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((p) => p[0].toUpperCase())
    .join('')
}

function fechaDeHoy() {
  const texto = new Date().toLocaleDateString('es-AR', { weekday: 'long', day: 'numeric', month: 'long' })
  return texto.charAt(0).toUpperCase() + texto.slice(1)
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
