import { Redraw, Ctrl, ViewModel, Agent, Facility } from './interfaces';

const TEAMS = ['a', 'b'];

export default function(redraw: Redraw): Ctrl {
  const vm: ViewModel = {
    state: 'connecting',
    selected: null,
    selectionIndex: 0,
  };

  const connect = function() {
    const ws = new WebSocket('ws://' + document.location.host + '/socket');

    ws.onmessage = function(msg) {
      const data = JSON.parse(msg.data);
      console.log(data);
      if (data.simId) vm.static = data;
      else vm.dynamic = data;
      redraw();
    };

    ws.onopen = function() {
      console.log('Connected');
      vm.state = 'online';
      redraw();
    };

    ws.onclose = function() {
      console.log('Disconnected');
      setTimeout(connect, 5000);
      vm.state = 'error';
      redraw();
    };
  };

  const entities = function(): Array<Agent | Facility> {
    const d = vm.dynamic;
    if (!d) return [];
    const list = ([] as Array<Agent | Facility>).concat(
      d.entities,
      d.workshops,
      d.dumps,
      d.resourceNodes,
      d.shops,
      d.chargingStations,
      d.storages
    );
    list.sort((a, b) => a.name.localeCompare(b.name));
    return list;
  };

  return {
    connect: connect,
    vm: vm,
    entities: entities,
    setSelection(name: string | null) {
      vm.selected = name;
      redraw();
    },
    selection: () => {
      if (!vm.selected) return null;
      return entities().filter(entity => entity.name === vm.selected)[0];
    },
    normalizeTeam(team: string) {
      if (vm.static) return TEAMS[vm.static.teams.indexOf(team)] || 'a';
      else return 'a';
    }
  };
}
