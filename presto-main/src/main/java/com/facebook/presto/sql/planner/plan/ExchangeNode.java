/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.sql.planner.plan;

import com.facebook.presto.sql.planner.Symbol;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

@Immutable
public class ExchangeNode
        extends PlanNode
{
    public static enum Type
    {
        GATHER,
        REPARTITION,
        REPLICATE
    }

    private final Type type;
    private final List<Symbol> outputs;

    private final List<PlanNode> sources;
    private final List<Symbol> partitionKeys;
    private final Optional<Symbol> hashSymbol;

    // for each source, the list of inputs corresponding to each output
    private final List<List<Symbol>> inputs;

    @JsonCreator
    public ExchangeNode(
            @JsonProperty("id") PlanNodeId id,
            @JsonProperty("type") Type type,
            @JsonProperty("partitionKeys") List<Symbol> partitionKeys,
            @JsonProperty("hashSymbol") Optional<Symbol> hashSymbol,
            @JsonProperty("sources") List<PlanNode> sources,
            @JsonProperty("outputs") List<Symbol> outputs,
            @JsonProperty("inputs") List<List<Symbol>> inputs)
    {
        super(id);

        checkNotNull(type, "type is null");
        checkNotNull(sources, "sources is null");
        checkNotNull(partitionKeys, "partitionKeys is null");
        checkNotNull(hashSymbol, "hashSymbol is null");
        checkNotNull(outputs, "outputs is null");
        checkNotNull(inputs, "inputs is null");

        this.type = type;
        this.sources = sources;
        this.partitionKeys = ImmutableList.copyOf(partitionKeys);
        this.hashSymbol = hashSymbol;
        this.outputs = ImmutableList.copyOf(outputs);
        this.inputs = ImmutableList.copyOf(inputs);
    }

    public static ExchangeNode partitionedExchange(PlanNodeId id, PlanNode child, List<Symbol> partitionKeys, Optional<Symbol> hashSymbol)
    {
        return new ExchangeNode(
                id,
                ExchangeNode.Type.REPARTITION,
                partitionKeys,
                hashSymbol,
                ImmutableList.of(child),
                child.getOutputSymbols(),
                ImmutableList.of(child.getOutputSymbols()));
    }

    public static ExchangeNode gatheringExchange(PlanNodeId id, PlanNode child)
    {
        return new ExchangeNode(
                id,
                ExchangeNode.Type.GATHER,
                ImmutableList.of(),
                Optional.<Symbol>empty(),
                ImmutableList.of(child),
                child.getOutputSymbols(),
                ImmutableList.of(child.getOutputSymbols()));
    }

    @JsonProperty
    public Type getType()
    {
        return type;
    }

    @Override
    public List<PlanNode> getSources()
    {
        return sources;
    }

    @Override
    @JsonProperty("outputs")
    public List<Symbol> getOutputSymbols()
    {
        return outputs;
    }

    @JsonProperty
    public List<Symbol> getPartitionKeys()
    {
        return partitionKeys;
    }

    @JsonProperty
    public Optional<Symbol> getHashSymbol()
    {
        return hashSymbol;
    }

    @JsonProperty
    public List<List<Symbol>> getInputs()
    {
        return inputs;
    }

    @Override
    public <C, R> R accept(PlanVisitor<C, R> visitor, C context)
    {
        return visitor.visitExchange(this, context);
    }
}
