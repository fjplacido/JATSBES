package br.pucrio.inf.les.jat.sample.books;

import java.util.Hashtable;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * This class was addapted from JADE examples package. It represents an agent in
 * JADE platform that acts as a book seller in a trade system.
 * 
 * @author Roberta
 * 
 */
public class BookSellerAgent extends Agent {

	private Hashtable catalogue;
	private String buyer;

	protected void setup() {
		super.setup();

		catalogue = new Hashtable();

		String targetBookTitle = null;
		int targetBookPrice = 0;

		Object[] args = getArguments();

		if (args != null && args.length >= 2) {
			System.out.println("ARGS >= 2");

			try {
				targetBookTitle = (String) args[0];

				targetBookPrice = Integer.parseInt((String) args[1]);
				catalogue.put(targetBookTitle, new Integer(targetBookPrice));

			} catch (NumberFormatException ex) {
				ex.printStackTrace();
			}

		}

		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("book-selling");
		sd.setName("JADE-book-trading");
		dfd.addServices(sd);

		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		addBehaviour(new OfferRequestsServer());
		addBehaviour(new PurchaseOrdersServer());
	}

	protected void takeDown() {

		try {
			DFService.deregister(this);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}

	/**
	 * Inner class OfferRequestsServer. This is the behaviour used by Book-seller
	 * agents to serve incoming requests for offer from buyer agents. If the
	 * requested book is in the local catalogue the seller agent replies with a
	 * PROPOSE message specifying the price. Otherwise a REFUSE message is sent
	 * back.
	 */
	private class OfferRequestsServer extends CyclicBehaviour {

		public void action() {

			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);

			if (msg != null) {

				String title = msg.getContent();
				ACLMessage reply = msg.createReply();

				Integer price = (Integer) catalogue.get(title);
				if (price != null) {
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent(String.valueOf(price.intValue()));
					myAgent.send(reply);
					System.out.println(
							myAgent.getAID().getName() + " enviando proposta para " + msg.getSender().getName());
				} else {
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("not-available");
					myAgent.send(reply);
				}

			} else {
				block();
			}
		}
	}

	/**
	 * Inner class PurchaseOrdersServer. This is the behaviour used by Book-seller
	 * agents to serve incoming offer acceptances (i.e. purchase orders) from buyer
	 * agents. The seller agent removes the purchased book from its catalogue and
	 * replies with an INFORM message to notify the buyer that the purchase has been
	 * sucesfully completed.
	 */
	private class PurchaseOrdersServer extends CyclicBehaviour {

		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
			ACLMessage msg = myAgent.receive(mt);

			if (msg != null) {

				String title = msg.getContent();
				ACLMessage reply = msg.createReply();

				Integer price = (Integer) catalogue.remove(title);
				if (price != null) {
					reply.setPerformative(ACLMessage.INFORM);
					myAgent.send(reply);
					buyer = msg.getSender().getLocalName();
					System.out.println("Livro vendido para " + msg.getSender().getName());
				} else {
					reply.setPerformative(ACLMessage.FAILURE);
					reply.setContent("not-available");
					myAgent.send(reply);
				}
			} else {
				block();
			}
		}
	}
}
