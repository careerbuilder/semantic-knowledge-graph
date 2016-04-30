package com.careerbuilder.search.relevancy;

import com.careerbuilder.search.relevancy.model.RequestNode;
import com.careerbuilder.search.relevancy.model.ResponseNode;

import java.io.IOException;

public interface RecursionOp {

    ResponseNode [] transform(NodeContext nodeContext, RequestNode [] request, ResponseNode [] responses) throws IOException;

}
