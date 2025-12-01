let processes = [];
let processIdCounter = 1;
const processColors = {};
const availableColors = [
  'bg-blue-500',
  'bg-emerald-500',
  'bg-violet-500',
  'bg-amber-500',
  'bg-rose-500',
  'bg-cyan-500',
  'bg-indigo-500',
  'bg-fuchsia-500',
];


function addProcess() {
  const arrivalInput = document.getElementById('inputArrival');
  const burstInput = document.getElementById('inputBurst');
  const priorityInput = document.getElementById('inputPriority');

  const arrival = parseInt(arrivalInput.value) || 0;
  const burst = parseInt(burstInput.value) || 1;
  const priority = parseInt(priorityInput.value) || 0;
  const id = `P${processIdCounter++}`;

  if (!processColors[id]) {
    processColors[id] =
      availableColors[(processIdCounter - 2) % availableColors.length];
  }

  const newProcess = {
    id: id,
    arrival: arrival,
    burst: burst,
    priority: priority,
  };

  processes.push(newProcess);


  burstInput.value = Math.floor(Math.random() * 8) + 1;
  priorityInput.value = Math.floor(Math.random() * 5);

  updateProcessTable();
}

function removeProcess(index) {
  processes.splice(index, 1);
  updateProcessTable();
}

function clearAll() {
  processes = [];
  processIdCounter = 1;
  updateProcessTable();

  document.getElementById('resultsContainer').classList.add('hidden');
  document.getElementById('welcomeMessage').classList.remove('hidden');
}

function updateProcessTable() {
  const tbody = document.getElementById('processTableBody');
  const emptyState = document.getElementById('emptyState');

  tbody.innerHTML = '';

  if (processes.length === 0) {
    emptyState.classList.remove('hidden');
  } else {
    emptyState.classList.add('hidden');
    processes.forEach((p, index) => {
      const tr = document.createElement('tr');
      tr.innerHTML = `
                <td><span class="process-badge">${p.id}</span></td>
                <td class="text-center" style="font-family: monospace;">${p.arrival}</td>
                <td class="text-center" style="font-family: monospace;">${p.burst}</td>
                <td class="text-center"><span class="prio-badge">${p.priority}</span></td>
                <td class="text-right">
                    <button onclick="removeProcess(${index})" class="icon-btn-small" title="Remover">
                        <i class="fa-solid fa-trash-can"></i>
                    </button>
                </td>
            `;
      tbody.appendChild(tr);
    });
  }
}

function loadExample() {
  clearAll();
  const exampleData = [
    { arrival: 0, burst: 7, priority: 2 },
    { arrival: 2, burst: 4, priority: 1 },
    { arrival: 4, burst: 1, priority: 3 },
    { arrival: 5, burst: 4, priority: 2 },
  ];

  exampleData.forEach((d) => {
    const id = `P${processIdCounter++}`;
    if (!processColors[id]) {
      processColors[id] =
        availableColors[(processIdCounter - 2) % availableColors.length];
    }
    processes.push({
      id: id,
      arrival: d.arrival,
      burst: d.burst,
      priority: d.priority,
    });
  });
  updateProcessTable();
}

function toggleInfoModal() {
  const modal = document.getElementById('infoModal');
  modal.classList.toggle('hidden');
}

// --- INTEGRAÇÃO COM BACKEND ---

async function simular() {
  if (processes.length === 0) {
    alert('Adicione processos antes de simular!');
    return;
  }

  const resultsContainer = document.getElementById('resultsContainer');
  const welcomeMessage = document.getElementById('welcomeMessage');
  const statusBanner = document.querySelector('.status-banner span');

  welcomeMessage.classList.add('hidden');
  resultsContainer.classList.remove('hidden');
  statusBanner.textContent = 'Processando simulação no servidor...';

  const algorithm = document.getElementById('algorithmSelect').value;
  // URL base do Spring Boot 
  const baseUrl = 'http://localhost:8080/api/escalonamento';

  let endpoint = '';
  if (algorithm === 'PRIORITY_PREEMPTIVE') {
    endpoint = `${baseUrl}/prioridade-com-preempcao`;
  } else {
    endpoint = `${baseUrl}/srtf`;
  }
  const payload = processes.map((p) => ({
    nomeProcesso: p.id,
    tempoChegada: p.arrival,
    duracao: p.burst,
    prioridade: p.priority,
  }));

  try {
    const response = await fetch(endpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      throw new Error(`Erro no servidor: ${response.status}`);
    }

    const data = await response.json();

    renderMetrics(data);
    renderGanttChart(data.historicoExecucao);
    renderResultsTable(data.processosProcessados);

    statusBanner.textContent = 'Simulação concluída com sucesso!';
  } catch (error) {
    console.error('Erro na simulação:', error);
    statusBanner.textContent =
      'Erro ao conectar com o backend. Verifique se o servidor Java está rodando.';
    alert('Erro ao conectar com o backend. Veja o console para detalhes.');
  }
}


function renderMetrics(data) {
  document.getElementById('avgWaitTime').textContent =
    data.tempoMedioEspera.toFixed(2);
  document.getElementById('avgTurnaroundTime').textContent =
    data.tempoMedioTurnaround.toFixed(2);
}

function renderResultsTable(processosBackend) {
  const tbody = document.getElementById('resultsTableBody');
  tbody.innerHTML = '';

  processosBackend.sort((a, b) => {
    const numA = parseInt(a.nomeProcesso.replace('P', ''));
    const numB = parseInt(b.nomeProcesso.replace('P', ''));
    return numA - numB;
  });

  processosBackend.forEach((p) => {
    const tr = document.createElement('tr');
    tr.innerHTML = `
            <td class="font-bold">${p.nomeProcesso}</td>
            <td class="text-center">${p.tempoChegada}</td>
            <td class="text-center">${p.duracao}</td>
            <td class="text-center text-blue-600 font-bold">${p.tempoEspera}</td>
            <td class="text-center text-emerald-600 font-bold">${p.tempoTurnaround}</td>
            <td class="text-center text-muted">${p.tempoConclusao}</td>
        `;
    tbody.appendChild(tr);
  });
}

function renderGanttChart(historico) {
  const container = document.getElementById('ganttChart');

  container.innerHTML = '';
  container.className = 'gantt-chart-container'; 
  container.style.display = 'flex';
  container.style.width = '100%';
  container.style.height = '60px';
  container.style.borderRadius = '8px';
  container.style.overflow = 'hidden';

  if (!historico || historico.length === 0) {
    container.textContent = 'Sem dados de execução.';
    return;
  }

  const blocks = [];
  let currentBlock = { id: historico[0], duration: 1 };

  for (let i = 1; i < historico.length; i++) {
    if (historico[i] === currentBlock.id) {
      currentBlock.duration++;
    } else {
      blocks.push(currentBlock);
      currentBlock = { id: historico[i], duration: 1 };
    }
  }
  blocks.push(currentBlock);

  const totalTime = historico.length;

  blocks.forEach((block) => {
    const div = document.createElement('div');
    const widthPercent = (block.duration / totalTime) * 100;

    div.style.width = `${widthPercent}%`;
    div.style.height = '100%';
    div.style.display = 'flex';
    div.style.alignItems = 'center';
    div.style.justifyContent = 'center';
    div.style.fontSize = '0.75rem';
    div.style.fontWeight = 'bold';
    div.style.color = '#fff';
    div.style.transition = 'all 0.2s';
    div.title = `${block.id} (Duração: ${block.duration})`;

    if (block.id === 'Ocioso') {
      div.style.background = '#e2e8f0'; 
      div.style.color = '#94a3b8'; 
      div.innerHTML = '<i class="fa-solid fa-pause"></i>';
    } else {
      const bgClass = processColors[block.id] || 'bg-slate-500';
      div.style.backgroundColor = getColorHex(bgClass);
      div.textContent = block.id;
      div.style.borderRight = '1px solid rgba(255,255,255,0.2)';
    }

    container.appendChild(div);
  });
}

function getColorHex(tailwindClass) {
  const map = {
    'bg-blue-500': '#3b82f6',
    'bg-emerald-500': '#10b981',
    'bg-violet-500': '#8b5cf6',
    'bg-amber-500': '#f59e0b',
    'bg-rose-500': '#f43f5e',
    'bg-cyan-500': '#06b6d4',
    'bg-indigo-500': '#6366f1',
    'bg-fuchsia-500': '#d946ef',
    'bg-slate-500': '#64748b',
  };
  return map[tailwindClass] || '#64748b';
}
