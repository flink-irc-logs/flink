package eu.stratosphere.nephele.streaming.latency;

import eu.stratosphere.nephele.executiongraph.ExecutionGraph;
import eu.stratosphere.nephele.executiongraph.ExecutionGroupVertex;
import eu.stratosphere.nephele.managementgraph.ManagementEdgeID;
import eu.stratosphere.nephele.managementgraph.ManagementVertexID;
import eu.stratosphere.nephele.streaming.types.ChannelLatency;
import eu.stratosphere.nephele.streaming.types.TaskLatency;


public class LatencyModel {

	// private static Log LOG = LogFactory.getLog(LatencyModel.class);

	private ExecutionGraph executionGraph;

	private LatencySubgraph latencySubgraph;

	public LatencyModel(ExecutionGraph executionGraph) {
		this.executionGraph = executionGraph;

		// FIXME naive implementation until we can annotate the job
		// subgraphStart and subgraphEnd should be derived from the annotations
		ExecutionGroupVertex subgraphStart = this.executionGraph.getInputVertex(0).getGroupVertex();
		ExecutionGroupVertex subgraphEnd = this.executionGraph.getOutputVertex(0).getGroupVertex();

		this.latencySubgraph = new LatencySubgraph(executionGraph, subgraphStart, subgraphEnd);
	}

	public int i = 0;

	public void refreshEdgeLatency(ChannelLatency channelLatency) {
		
		// FIXME: this is done to prevent an NPE caused by another bug that causes identical 
		// vertex ids inside the channel latency
		ManagementVertexID startID = channelLatency.getSourceVertexID().toManagementVertexID();
		ManagementVertexID stopID = channelLatency.getSinkVertexID().toManagementVertexID();
		if(startID.equals(stopID)) {
			return;
		}
		
		ManagementEdgeID edgeID = new ManagementEdgeID(channelLatency.getSourceVertexID().toManagementVertexID(),
				channelLatency.getSinkVertexID().toManagementVertexID());

		EdgeLatency edgeLatency = latencySubgraph.getEdgeLatency(edgeID);
		edgeLatency.setLatencyInMillis(channelLatency.getChannelLatency());
		
		i++;
		
		if(i % 20 == 0) {
			for (LatencyPath path : latencySubgraph.getLatencyPaths()) {
				path.dumpLatencies();
			}
		}
	}

	public void refreshTaskLatency(TaskLatency taskLatency) {
		VertexLatency vertexLatency = latencySubgraph
			.getVertexLatency(taskLatency.getVertexID().toManagementVertexID());
		vertexLatency.setLatencyInMillis(taskLatency.getTaskLatency());
		i++;
		
		if(i % 20 == 0) {
			for (LatencyPath path : latencySubgraph.getLatencyPaths()) {
				path.dumpLatencies();
			}
		}
	}
}
