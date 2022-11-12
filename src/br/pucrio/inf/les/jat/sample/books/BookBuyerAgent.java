package br.pucrio.inf.les.jat.sample.books;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * This class was addapted from JADE examples package. It Represents an agent in
 * JADE platform that acts as a book byer in a trade system.
 * 
 * @author roberta
 * 
 */

public class BookBuyerAgent extends Agent {

	protected String targetBookTitle;
	protected AID[] sellerAgents;
	private String best;

	protected void setup() {

		System.out.println("Hallo! Buyer-agent " + getAID().getName() + " is ready.");

		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			targetBookTitle = (String) args[0];
			// addBehaviour(new BookBuyerTickerBehaviour(this,10000));

			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("book-selling");
			template.addServices(sd);
			try {
				DFAgentDescription[] result = DFService.search(this, template);
				System.out.println("Found the following seller agents:");
				sellerAgents = new AID[result.length];
				for (int i = 0; i < result.length; ++i) {
					sellerAgents[i] = result[i].getName();
					System.out.println(sellerAgents[i].getName());
				}
			} catch (FIPAException fe) {
				fe.printStackTrace();
			}

			addBehaviour(new RequestPerformer());
		} else {
			doDelete();
		}
	}

	protected void takeDown() {
		System.out.println("Buyer-agent " + getAID().getName() + " terminating.");
	}

	/*
	 * private class BookBuyerTickerBehaviour extends TickerBehaviour {
	 * 
	 * public BookBuyerTickerBehaviour(Agent agent,int time) { super(agent,time); }
	 * 
	 * protected void onTick() {
	 * 
	 * DFAgentDescription template = new DFAgentDescription(); ServiceDescription sd
	 * = new ServiceDescription(); sd.setType("book-selling");
	 * template.addServices(sd); try { DFAgentDescription[] result =
	 * DFService.search(myAgent, template);
	 * System.out.println("Found the following seller agents:"); sellerAgents = new
	 * AID[result.length]; for (int i = 0; i < result.length; ++i) { sellerAgents[i]
	 * = result[i].getName(); System.out.println(sellerAgents[i].getName()); } }
	 * catch (FIPAException fe) { fe.printStackTrace(); }
	 * 
	 * myAgent.addBehaviour(new RequestPerformer()); } }
	 */

	/**
	 * Inner class RequestPerformer. This is the behaviour used by Book-buyer agents
	 * to request seller agents the target book.
	 */
	private class RequestPerformer extends Behaviour {
		private AID bestSeller;
		private int bestPrice;
		private int repliesCnt = 0;
		private MessageTemplate mt;
		private int step = 0;

		public void action() {
			switch (step) {
			case 0:
				ACLMessage cfp = sendCFP();
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				step = 1;
				break;
			case 1:
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					if (reply.getPerformative() == ACLMessage.PROPOSE) {
						int price = Integer.parseInt(reply.getContent());
						if (bestSeller == null || price < bestPrice) {
							bestPrice = price;
							bestSeller = reply.getSender();
						}
					}
					repliesCnt++;
					if (repliesCnt >= sellerAgents.length) {
						step = 2;
					}
				} else {
					block();
				}
				break;
			case 2:
				ACLMessage order = sendACCEPT(bestSeller);
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
						MessageTemplate.MatchInReplyTo(order.getReplyWith()));
				step = 3;

				break;
			case 3:
				reply = myAgent.receive(mt);
				if (reply != null) {
					if (reply.getPerformative() == ACLMessage.INFORM) {
						System.out.println(
								targetBookTitle + " successfully purchased from agent " + reply.getSender().getName());
						System.out.println("Price = " + bestPrice);
						best = reply.getSender().getLocalName();
					} else {
						System.out.println("Attempt failed: requested book already sold.");
					}
					step = 4;
				} else {
					block();
				}
				break;
			}
		}

		public boolean done() {
			if (step == 2 && bestSeller == null) {
				System.out.println("Attempt failed: " + targetBookTitle + " not available for sale");
			}
			return ((step == 2 && bestSeller == null) || step == 4);
		}
	}

	private ACLMessage sendCFP() {

		ACLMessage cfp = new ACLMessage(ACLMessage.CFP);

		// Se sellerAgents for null pode ocorrer um erro não verifica do aqui.
		for (int i = 0; i < sellerAgents.length; ++i) {
			cfp.addReceiver(sellerAgents[i]);
		}
		cfp.setContent(targetBookTitle);
		cfp.setConversationId("book-trade");
		cfp.setReplyWith("cfp" + System.currentTimeMillis());
		send(cfp);

		return cfp;
	}

	private ACLMessage sendACCEPT(AID bestSeller) {
		ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
		order.addReceiver(bestSeller);
		order.setContent(targetBookTitle);
		order.setConversationId("book-trade");
		order.setReplyWith("order" + System.currentTimeMillis());
		send(order);

		return order;
	}
}
