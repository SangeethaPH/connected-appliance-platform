import { FormEvent, ReactNode, useCallback, useEffect, useMemo, useState } from 'react'
import { infraApi, simulatorApi } from './api'
import type { Appliance, CustomReport, DailyReport, InfraVendor, Metric, RawEvent, SimAppliance, Vendor } from './types'

type Tab = 'simulator' | 'infrastructure' | 'reports'
const applianceTypes = ['REFRIGERATOR','AIR_CONDITIONER','OVEN','WASHER','DRYER','TELEVISION','FAN']

function App() {
  const [tab,setTab] = useState<Tab>('simulator')
  const [notice,setNotice] = useState('')
  const [error,setError] = useState('')
  const run = async <T,>(work:()=>Promise<T>, message:string) => {
    setError(''); try { const result=await work(); setNotice(message); return result }
    catch(e) { setError(e instanceof Error ? e.message : 'Request failed'); throw e }
  }
  return <div className="shell">
    <aside>
      <div className="brand"><span>⌁</span><div>APPLIANCE<small>OPERATIONS</small></div></div>
      <nav>
        <button className={tab==='simulator'?'active':''} onClick={()=>setTab('simulator')}><b>◈</b> Vendor Simulator</button>
        <button className={tab==='infrastructure'?'active':''} onClick={()=>setTab('infrastructure')}><b>⌘</b> Infrastructure</button>
        <button className={tab==='reports'?'active':''} onClick={()=>setTab('reports')}><b>▥</b> Reports</button>
      </nav>
      <div className="system"><i></i><div>LOCAL SYSTEM<small>Kafka · PostgreSQL</small></div></div>
    </aside>
    <main>
      <header><div><p>CONNECTED HOME / {tab.toUpperCase()}</p><h1>{tab==='simulator'?'Vendor Simulator':tab==='infrastructure'?'Infrastructure Manager':'Reports & Analytics'}</h1></div><span className="live"><i></i> SYSTEM LIVE</span></header>
      {error && <div className="toast error">{error}<button onClick={()=>setError('')}>×</button></div>}
      {notice && <div className="toast">{notice}<button onClick={()=>setNotice('')}>×</button></div>}
      {tab==='simulator' && <Simulator run={run}/>}
      {tab==='infrastructure' && <Infrastructure run={run}/>}
      {tab==='reports' && <Reports run={run}/>}
    </main>
  </div>
}

function Simulator({run}:{run:<T>(w:()=>Promise<T>,m:string)=>Promise<T>}) {
  const [vendors,setVendors]=useState<Vendor[]>([]), [appliances,setAppliances]=useState<SimAppliance[]>([]), [events,setEvents]=useState<RawEvent[]>([])
  const [vendorForm,setVendorForm]=useState({id:'',name:'',apiStyle:'REST/JSON',authentication:'BASIC',metricProfile:'ACME'})
  const [device,setDevice]=useState({vendorId:'acme',name:'Kitchen Refrigerator',type:'REFRIGERATOR',metricIntervalSeconds:10,emissionMode:'MANUAL_ONLY'})
  const load=useCallback(async()=>{setVendors(await simulatorApi.get('/api/vendors'));setAppliances(await simulatorApi.get('/api/simulator/appliances'))},[])
  useEffect(()=>{load().catch(()=>{})},[load])
  const createVendor=async(e:FormEvent)=>{e.preventDefault();const payload={...vendorForm,id:vendorForm.id.trim(),name:vendorForm.name.trim()};await run(()=>simulatorApi.post('/api/vendors',payload),'Simulated vendor created');setVendorForm({...vendorForm,id:'',name:''});await load()}
  const createDevice=async(e:FormEvent)=>{e.preventDefault();await run(()=>simulatorApi.post('/api/simulator/appliances',device),'Appliance created in simulator');await load()}
  const emit=async(id:string)=>{const event=await run(()=>simulatorApi.post<RawEvent>(`/api/simulator/appliances/${id}/emit`),'Event published to Kafka');setEvents(x=>[event,...x].slice(0,10));await load()}
  const reset=async()=>{if(!confirm('Clear all simulator appliances, metrics, session events, and custom vendors?'))return;await run(()=>simulatorApi.delete('/api/simulator/data'),'Simulator data reset');setEvents([]);await load()}
  return <>
    <section className="stats"><Stat label="VENDOR PROFILES" value={vendors.length}/><Stat label="SIMULATED DEVICES" value={appliances.length}/><Stat label="SESSION EVENTS" value={events.length}/></section>
    <div className="grid two">
      <Panel title="Create vendor" eyebrow="VENDOR CATALOG">
        <form onSubmit={createVendor} className="form-grid">
          <Field label="Vendor code"><input required value={vendorForm.id} onChange={e=>setVendorForm({...vendorForm,id:e.target.value})} placeholder="e.g. nova"/></Field>
          <Field label="Vendor name"><input required value={vendorForm.name} onChange={e=>setVendorForm({...vendorForm,name:e.target.value})} placeholder="Nova Appliances"/></Field>
          <Field label="Authentication"><select value={vendorForm.authentication} onChange={e=>setVendorForm({...vendorForm,authentication:e.target.value})}><option>BASIC</option><option>API_KEY</option><option>OAUTH2</option></select></Field>
          <Field label="Metric profile"><select value={vendorForm.metricProfile} onChange={e=>setVendorForm({...vendorForm,metricProfile:e.target.value})}><option value="ACME">ACME compatible</option><option value="GLOBEX">GLOBEX compatible</option></select></Field>
          <ProfileHint profile={vendorForm.metricProfile}/>
          <button className="primary wide">Create vendor</button>
        </form>
        <details className="profile-guide">
          <summary>ⓘ Which metric profile should I choose?</summary>
          <div className="profile-options">
            <div><span className="profile-code acme">ACME</span><b>Standard metric format</b><p>Choose when the vendor sends power in <strong>watts</strong>, temperatures in <strong>Celsius</strong>, and percentages from 0–100.</p><code>power_watts · internal_temp_c</code></div>
            <div><span className="profile-code globex">GLOBEX</span><b>Alternative metric format</b><p>Choose when the vendor sends power in <strong>kilowatts</strong>, temperatures in <strong>Fahrenheit</strong>, and progress as a 0–1 ratio.</p><code>energy_kw · cabinet_temperature_f</code></div>
          </div>
          <p className="guide-note">These profiles describe the vendor's raw metric format—not the vendor name. A custom vendor such as Nova can choose either profile based on the data it emits.</p>
        </details>
      </Panel>
      <Panel title="Create appliance" eyebrow="DEVICE EMULATOR">
        <form onSubmit={createDevice} className="form-grid">
          <Field label="Vendor"><select value={device.vendorId} onChange={e=>setDevice({...device,vendorId:e.target.value})}>{vendors.map(v=><option key={v.id} value={v.id}>{v.name}</option>)}</select></Field>
          <Field label="Device name"><input required value={device.name} onChange={e=>setDevice({...device,name:e.target.value})}/></Field>
          <Field label="Appliance type"><select value={device.type} onChange={e=>setDevice({...device,type:e.target.value})}>{applianceTypes.map(t=><option key={t}>{t}</option>)}</select></Field>
          <Field label="Emission mode"><select value={device.emissionMode} onChange={e=>setDevice({...device,emissionMode:e.target.value})}><option>MANUAL_ONLY</option><option>AUTOMATIC</option></select></Field>
          <button className="primary wide">Create appliance</button>
        </form>
      </Panel>
    </div>
    <Panel title="Simulated appliances" eyebrow="EMISSION CONTROL" action={<button className="danger" onClick={reset}>Reset simulator</button>}>
      <div className="cards">{appliances.map(a=><article className="device" key={a.id}><div className="device-head"><span className="device-icon">{icon(a.type)}</span><span className="pill">{a.vendorId}</span></div><h3>{a.name}</h3><p>{a.type.replaceAll('_',' ')} · {a.emissionMode}</p><small>{a.id}</small><button className="emit" onClick={()=>emit(a.id)}>Emit event <span>→</span></button></article>)}{!appliances.length&&<Empty text="Create the first simulated appliance"/>}</div>
    </Panel>
    <Panel title="Recently emitted events" eyebrow="KAFKA PRODUCER">
      <div className="event-list">{events.map(e=>{const appliance=appliances.find(a=>a.id===e.externalApplianceId);return <div className="event" key={e.eventId}><i></i><div><b>{appliance?.name||'Unknown appliance'}</b><small>{e.vendorCode} / {e.applianceType} · {e.eventId}</small></div><div className="metric-chips">{Object.entries(e.metrics).map(([k,v])=><span key={k}>{k}: <b>{v}</b></span>)}</div></div>})}{!events.length&&<Empty text="Manual emissions will appear here"/>}</div>
    </Panel>
  </>
}

function Infrastructure({run}:{run:<T>(w:()=>Promise<T>,m:string)=>Promise<T>}) {
  const [vendors,setVendors]=useState<InfraVendor[]>([]),[appliances,setAppliances]=useState<Appliance[]>([]),[metrics,setMetrics]=useState<Metric[]>([]),[selected,setSelected]=useState(''),[live,setLive]=useState(true)
  const [vendor,setVendor]=useState({code:'',name:'',baseUrl:'http://localhost:8081',username:'',password:''})
  const [onboard,setOnboard]=useState({vendorId:'',externalApplianceId:''})
  const load=useCallback(async()=>{const [v,a]=await Promise.all([infraApi.get<InfraVendor[]>('/api/vendors'),infraApi.get<Appliance[]>('/api/appliances')]);setVendors(v);setAppliances(a);if(!selected&&a[0])setSelected(a[0].id)},[selected])
  const loadMetrics=useCallback(async()=>{if(selected)setMetrics(await infraApi.get(`/api/appliances/${selected}/metrics`))},[selected])
  useEffect(()=>{load().catch(()=>{})},[load])
  useEffect(()=>{loadMetrics().catch(()=>{});if(!live)return;const timer=setInterval(()=>loadMetrics().catch(()=>{}),3000);return()=>clearInterval(timer)},[loadMetrics,live])
  const register=async(e:FormEvent)=>{e.preventDefault();await run(()=>infraApi.post('/api/vendors',vendor),'Vendor registered securely');setVendor({...vendor,code:'',name:''});await load()}
  const onboardDevice=async(e:FormEvent)=>{e.preventDefault();await run(()=>infraApi.post(`/api/vendors/${onboard.vendorId}/appliances`,{externalApplianceId:onboard.externalApplianceId}),'Appliance onboarded');setOnboard({...onboard,externalApplianceId:''});await load()}
  const clearData=async()=>{if(!confirm('Permanently clear all registered vendors, appliances, metric history, and reports from PostgreSQL?'))return;await run(()=>infraApi.delete('/api/admin/demo-data'),'Infrastructure demo data cleared');setMetrics([]);setSelected('');await load()}
  const latest=useMemo(()=>{const map=new Map<string,Metric>();metrics.forEach(m=>map.set(m.metricName,m));return [...map.values()]},[metrics])
  return <>
    <section className="stats"><Stat label="REGISTERED VENDORS" value={vendors.length}/><Stat label="ONBOARDED APPLIANCES" value={appliances.length}/><Stat label="METRIC SAMPLES" value={metrics.length}/></section>
    <div className="grid two">
      <Panel title="Register vendor" eyebrow="BASIC AUTH CONNECTOR"><form onSubmit={register} className="form-grid"><Field label="Vendor code"><input required value={vendor.code} onChange={e=>setVendor({...vendor,code:e.target.value})}/></Field><Field label="Vendor name"><input required value={vendor.name} onChange={e=>setVendor({...vendor,name:e.target.value})}/></Field><Field label="Simulator base URL"><input required value={vendor.baseUrl} onChange={e=>setVendor({...vendor,baseUrl:e.target.value})}/></Field><Field label="Username"><input required value={vendor.username} onChange={e=>setVendor({...vendor,username:e.target.value})}/></Field><Field label="Password"><input required type="password" value={vendor.password} onChange={e=>setVendor({...vendor,password:e.target.value})}/></Field><button className="primary wide">Register vendor</button></form></Panel>
      <Panel title="Onboard appliance" eyebrow="INVENTORY"><form onSubmit={onboardDevice} className="form-grid"><Field label="Registered vendor"><select required value={onboard.vendorId} onChange={e=>setOnboard({...onboard,vendorId:e.target.value})}><option value="">Select vendor</option>{vendors.map(v=><option key={v.id} value={v.id}>{v.name} ({v.code})</option>)}</select></Field><Field label="Simulator appliance ID"><input required value={onboard.externalApplianceId} onChange={e=>setOnboard({...onboard,externalApplianceId:e.target.value})} placeholder="Paste external appliance ID"/></Field><button className="primary wide">Verify & onboard</button></form><p className="hint">The manager contacts the simulator, verifies the device, and stores the vendor mapping.</p></Panel>
    </div>
    <Panel title="Onboarded appliances" eyebrow="PERSISTENT INVENTORY" action={<button className="danger" onClick={clearData}>Clear all data</button>}><div className="table-wrap"><table><thead><tr><th>Appliance</th><th>Type</th><th>Status</th><th>External ID</th><th></th></tr></thead><tbody>{appliances.map(a=><tr key={a.id} className={selected===a.id?'selected':''}><td><b>{a.name}</b><small>{a.id}</small></td><td>{a.type}</td><td><span className="status"><i></i>{a.status}</span></td><td className="mono">{a.externalId}</td><td><button className="link" onClick={()=>setSelected(a.id)}>View metrics</button></td></tr>)}</tbody></table>{!appliances.length&&<Empty text="No appliances onboarded yet"/>}</div></Panel>
    <Panel title="Live metrics & history" eyebrow="POSTGRESQL TIME SERIES" action={<label className="toggle"><input type="checkbox" checked={live} onChange={e=>setLive(e.target.checked)}/><span></span> Live refresh</label>}>
      <div className="latest">{latest.map(m=><div key={m.metricName}><small>{m.metricName}</small><strong>{m.value}<em>{m.unit}</em></strong><span>{new Date(m.capturedAt).toLocaleTimeString()}</span></div>)}</div>
      <div className="table-wrap"><table><thead><tr><th>Captured</th><th>Metric</th><th>Value</th><th>Event ID</th></tr></thead><tbody>{[...metrics].reverse().map(m=><tr key={m.id}><td>{new Date(m.capturedAt).toLocaleString()}</td><td><b>{m.metricName}</b></td><td>{m.value} {m.unit}</td><td className="mono">{m.eventId}</td></tr>)}</tbody></table>{!metrics.length&&<Empty text="Select an appliance or emit its first event"/>}</div>
    </Panel>
  </>
}

function Reports({run}:{run:<T>(w:()=>Promise<T>,m:string)=>Promise<T>}) {
  const initial=rangePreset('hour')
  const [daily,setDaily]=useState<DailyReport[]>([]),[custom,setCustom]=useState<CustomReport[]>([]),[date,setDate]=useState(new Date().toISOString().slice(0,10)),[from,setFrom]=useState(initial.from),[to,setTo]=useState(initial.to)
  const load=useCallback(async()=>{const [d,c]=await Promise.all([infraApi.get<DailyReport[]>('/api/reports/daily'),infraApi.get<CustomReport[]>('/api/reports/custom')]);setDaily(d);setCustom(c)},[])
  useEffect(()=>{load().catch(()=>{})},[load])
  const createDaily=async(e:FormEvent)=>{e.preventDefault();await run(()=>infraApi.post(`/api/reports/daily/${date}`),'Daily report generated');await load()}
  const createCustom=async(e:FormEvent)=>{e.preventDefault();await run(()=>infraApi.post('/api/reports/custom',{from:new Date(from).toISOString(),to:new Date(to).toISOString()}),'Custom report generated');await load()}
  return <>
    <section className="stats"><Stat label="DAILY REPORTS" value={daily.length}/><Stat label="CUSTOM REPORTS" value={custom.length}/><Stat label="TOTAL SAMPLES REPORTED" value={[...daily,...custom].reduce((n,r)=>n+r.totalSamples,0)}/></section>
    <div className="grid two"><Panel title="Daily report" eyebrow="ASIA / KOLKATA"><form onSubmit={createDaily} className="inline-form"><Field label="Report date"><input type="date" required value={date} onChange={e=>setDate(e.target.value)}/></Field><button className="primary">Generate</button></form><p className="hint">Aggregates count, minimum, maximum, and average for the selected day.</p></Panel><Panel title="Custom range report" eyebrow="ON DEMAND"><div className="range-presets"><button onClick={()=>applyRange('hour',setFrom,setTo)}>Last hour</button><button onClick={()=>applyRange('today',setFrom,setTo)}>Today</button><button onClick={()=>applyRange('day',setFrom,setTo)}>Last 24 hours</button></div><form onSubmit={createCustom} className="form-grid"><Field label="From — calendar & time"><input type="datetime-local" required value={from} onChange={e=>setFrom(e.target.value)}/></Field><Field label="To — calendar & time"><input type="datetime-local" required value={to} onChange={e=>setTo(e.target.value)}/></Field><button className="primary wide">Generate range report</button></form></Panel></div>
    <Panel title="Generated reports" eyebrow="PERSISTED REPORT ARCHIVE"><div className="report-grid">{daily.map(r=><ReportCard key={r.id} title={r.reportDate} type="DAILY" report={r}/>) }{custom.map(r=><ReportCard key={r.id} title={`${new Date(r.from).toLocaleDateString()} – ${new Date(r.to).toLocaleDateString()}`} type="CUSTOM" report={r}/>)}{!daily.length&&!custom.length&&<Empty text="Generate the first report"/>}</div></Panel>
  </>
}

function ReportCard({title,type,report}:{title:string;type:string;report:DailyReport|CustomReport}) {const filename=`${type.toLowerCase()}-report-${title.replaceAll(/[^a-zA-Z0-9-]/g,'-')}.json`;return <article className="report"><div><span className="pill">{type}</span><small>{new Date(report.generatedAt).toLocaleString()}</small></div><h3>{title}</h3><strong>{report.totalSamples}<em> samples</em></strong><p>{report.appliances.length} appliance{report.appliances.length===1?'':'s'}</p>{report.appliances.slice(0,2).map(a=><div className="summary" key={a.applianceId}><b>{a.applianceName}</b><span>Vendor: {a.vendorName} ({a.vendorCode})</span><span>Infrastructure ID: {a.applianceId}</span><span>External ID: {a.externalApplianceId}</span>{a.metrics.slice(0,2).map(m=><span key={m.metricName}>{m.metricName}: {m.average} {m.unit}</span>)}</div>)}<button className="download" onClick={()=>downloadJson(report,filename)}>↓ Download JSON</button></article>}
function Panel({title,eyebrow,action,children}:{title:string;eyebrow:string;action?:ReactNode;children:ReactNode}) {return <section className="panel"><div className="panel-title"><div><small>{eyebrow}</small><h2>{title}</h2></div>{action}</div>{children}</section>}
function Field({label,children}:{label:string;children:ReactNode}) {return <label className="field"><span>{label}</span>{children}</label>}
function Stat({label,value}:{label:string;value:number}) {return <div className="stat"><small>{label}</small><strong>{value}</strong><i></i></div>}
function Empty({text}:{text:string}) {return <div className="empty">◇<span>{text}</span></div>}
function ProfileHint({profile}:{profile:string}) {return <div className="profile-hint wide"><b>{profile==='ACME'?'Watts · Celsius · Percent':'Kilowatts · Fahrenheit · Ratio'}</b><span>{profile==='ACME'?'Best for vendors emitting standard canonical units.':'Best for vendors whose raw units need conversion during ingestion.'}</span></div>}
function icon(type:string){return ({REFRIGERATOR:'▥',AIR_CONDITIONER:'❄',OVEN:'◉',WASHER:'◌',DRYER:'◍',TELEVISION:'▰',FAN:'✣'} as Record<string,string>)[type]||'◇'}
function downloadJson(data:unknown,filename:string){const url=URL.createObjectURL(new Blob([JSON.stringify(data,null,2)],{type:'application/json'}));const link=document.createElement('a');link.href=url;link.download=filename;link.click();URL.revokeObjectURL(url)}
function localDateTime(d:Date){const p=(n:number)=>String(n).padStart(2,'0');return `${d.getFullYear()}-${p(d.getMonth()+1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}`}
function rangePreset(kind:'hour'|'today'|'day'){const to=new Date(),from=new Date(to);if(kind==='hour')from.setHours(from.getHours()-1);if(kind==='day')from.setDate(from.getDate()-1);if(kind==='today')from.setHours(0,0,0,0);return{from:localDateTime(from),to:localDateTime(to)}}
function applyRange(kind:'hour'|'today'|'day',setFrom:(v:string)=>void,setTo:(v:string)=>void){const r=rangePreset(kind);setFrom(r.from);setTo(r.to)}
export default App
