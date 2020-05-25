package massim.eismassim.entities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

import org.json.JSONException;
import org.json.JSONObject;

import eis.exceptions.PerceiveException;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.ParameterList;
import eis.iilang.Percept;
import massim.eismassim.Entity;
import massim.protocol.messages.Message;
import massim.protocol.messages.StatusRequestMessage;
import massim.protocol.messages.StatusResponseMessage;

public class StatusEntity extends Entity {

    private String host;
    private int port;

    public StatusEntity(String name, String host, int port) {
        super(name);
        this.host = host;
        this.port = port;
    }

    public static StatusResponseMessage queryServerStatus(String host, int port) {

        Message result = null;
        try (
            var socket = new Socket(host, port);
            var out = socket.getOutputStream();
            var in = socket.getInputStream();
            ) {
            var statusRequest = new StatusRequestMessage();
            var osw = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            osw.write(statusRequest.toJson().toString());
            osw.write(0);
            osw.flush();

            var buffer = new ByteArrayOutputStream();
            int read;
            while ((read = in.read()) != 0) {
                if (read == -1)
                    throw new IOException();
                buffer.write(read);
            }
            String message = buffer.toString(StandardCharsets.UTF_8);
            try {
                result = Message.buildFromJson(new JSONObject(message));
                if (result instanceof StatusResponseMessage) return (StatusResponseMessage) result;
            } catch (JSONException e) {}
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void run() {
        // no main loop required
    }

    @Override
    public LinkedList<Percept> getAllPercepts() throws PerceiveException {
        var status = queryServerStatus(host, port);
        var percepts = new LinkedList<Percept>();
        if (status != null) {
            var teams = new ParameterList();
            for (var team : status.teams) {
                teams.add(new Identifier(team));
            }
            percepts.add(new Percept("teams", teams));

            var teamSizes = new ParameterList();
            for (var teamSize : status.teamSizes) {
                teamSizes.add(new Numeral(teamSize));
            }
            percepts.add(new Percept("teamSizes", teams));

            percepts.add(new Percept("currentSim", new Numeral(status.currentSimulation)));

            int currentIndex = Math.max(status.currentSimulation, 0);
            percepts.add(new Percept("currentTeamSize", new Numeral(status.teamSizes[currentIndex])));
        }
        else{
            percepts.add(new Percept("error"));
        }
        return percepts;
    }
}