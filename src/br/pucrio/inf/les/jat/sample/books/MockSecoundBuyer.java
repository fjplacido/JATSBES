package br.pucrio.inf.les.jat.sample.books;

import br.pucrio.inf.les.jat.core.JadeMockAgent;
import br.pucrio.inf.les.jat.core.exception.ReplyReceptionFailed;
import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class MockSecoundBuyer extends JadeMockAgent {

	protected void setup() {
		addBehaviour(new TestBehaviour());
	}

	protected void takeDown() {

	}

	private class TestBehaviour extends OneShotBehaviour {

		public TestBehaviour() {

		}

		public void action() {
			ACLMessage msg;
			try {
				sendMessage(ACLMessage.CFP, new AID("BookSeller", AID.ISLOCALNAME), "Programação Modular");
				msg = blockReceiveMessage(30000, ACLMessage.PROPOSE);
				sendMessage(ACLMessage.ACCEPT_PROPOSAL, new AID("BookSeller", AID.ISLOCALNAME), "Programação Modular");
				msg = blockReceiveMessage(30000, ACLMessage.FAILURE);

				setTestResult("OK");
			} catch (ReplyReceptionFailed e) {
				setTestResult(prepareMessageResult(e));
			}
		}
	}
}