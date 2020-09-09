import styled from "styled-components";

// eslint-disable-next-line
const EmailRegex = /^([^\x00-\x20\x22\x28\x29\x2c\x2e\x3a-\x3c\x3e\x40\x5b-\x5d\x7f-\xff]+|\x22([^\x0d\x22\x5c\x80-\xff]|\x5c[\x00-\x7f])*\x22)(\x2e([^\x00-\x20\x22\x28\x29\x2c\x2e\x3a-\x3c\x3e\x40\x5b-\x5d\x7f-\xff]+|\x22([^\x0d\x22\x5c\x80-\xff]|\x5c[\x00-\x7f])*\x22))*\x40([^\x00-\x20\x22\x28\x29\x2c\x2e\x3a-\x3c\x3e\x40\x5b-\x5d\x7f-\xff]+|\x5b([^\x0d\x5b-\x5d\x80-\xff]|\x5c[\x00-\x7f])*\x5d)(\x2e([^\x00-\x20\x22\x28\x29\x2c\x2e\x3a-\x3c\x3e\x40\x5b-\x5d\x7f-\xff]+|\x5b([^\x0d\x5b-\x5d\x80-\xff]|\x5c[\x00-\x7f])*\x5d))*$/

const Card = styled.div`
    box-sizing: border-box;
    max-width: 410px;
    margin: 0 auto;
    padding: 0 2rem;
    display: flex;
    flex-direction: column;
    align-items: center;
`;

const Form = styled.div`
    display: flex;
    flex-direction: column;
    width: 100%;
`;

const Input = styled.input`
    display: flex;
    flex-direction: column;
    width: 100%
`;

const Button = styled.button`
    background: linear-gradient(to bottom, #6371c7, #5563c1);
    border-color: #3f4eae;
    border-radius: 3px;
    padding: 1rem;
    color: white;
    font-weight: 700;
    width: 100%;
    margin-bottom: 1rem;
    font-size: 0.8rem;
`;

const Logo = styled.img`
    width: 50%;
    margin-bottom: 1rem;
`;

const Error = styled.div`
    background-color: red;
`;

const Success = styled.div`
    backgroupd-color: green;
`;

export { Form, Input, Button, Logo, Card, Error, Success, EmailRegex };
