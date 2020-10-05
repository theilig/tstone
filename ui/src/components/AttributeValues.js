import React from "react";
import styled from "styled-components";

const AttributeContainer = styled.div`
    display: flex;
    flex-direction: row;
`;

function AttributeValues(props) {
    return (
        <AttributeContainer>
                {Object.keys(props.show).map((key) => {
                    const v = props.show[key]
                    return (<div>{key}:{props.values[v]}</div>)
                })}
        </AttributeContainer>
    )
}

export default AttributeValues