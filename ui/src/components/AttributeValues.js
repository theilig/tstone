import React from "react";
import styled from "styled-components";

const AttributeContainer = styled.div`
    display: flex;
    flex-direction: row;
    margin-left: 10px;
`;

function AttributeValues(props) {
    return (
        <AttributeContainer>
                {Object.keys(props.show).map((key) => {
                    const label = props.show[key]
                    return (<div style={{marginLeft: '5px'}} key={key}>{label}:{props.values[key]}</div>)
                })}
        </AttributeContainer>
    )
}

export default AttributeValues