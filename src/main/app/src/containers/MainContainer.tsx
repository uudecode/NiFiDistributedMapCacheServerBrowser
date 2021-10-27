import React, {SyntheticEvent, useState, KeyboardEvent} from "react";
import {Container, Header, Content, Form, Button,  Pagination, Schema } from "rsuite";

import { Table, Column, Cell, HeaderCell, ColumnGroup, TableProps } from 'rsuite-table';
import axios from "axios";

const TextField = (props: any) => {
    const { name, label, accepter, ...rest } = props;
    return (
        <Form.Group controlId={`${name}`}>
            <Form.ControlLabel>{label} </Form.ControlLabel>
            <Form.Control name={name} accepter={accepter} {...rest} />
        </Form.Group>
    );
}
const MainContainer = () => {
    const model = Schema.Model({
        host: Schema.Types.StringType().isRequired('This field is required.'),
        port: Schema.Types.NumberType().isRequired('This field is required.'),
        pattern: Schema.Types.StringType(),
    });

    const [loading, setLoading] = useState(false);
    const [data, setData] = useState([]);
    const [total, setTotal] = useState(0);
    const [pageSize, setPageSize] = useState(10);
    const [pageNumber, setPageNumber] = useState(1);
    const handleSubmit = (e: SyntheticEvent<Element, Event>) => {
        e.preventDefault();
        queryData();
    };
    const handlerKeys = (e: KeyboardEvent<HTMLFormElement>) => {
        if (e.key === "Enter") {
            queryData();
        }
    };
    const [inputText, setInputText] = useState({ host: "", port: "", pattern: "" });
    const onChange = (e: Record<string, string>) => {
        setInputText({
            ...inputText,
            host: e.host,
            port: e.port,
            pattern: e.pattern,
        });
    }

    const queryData = () => {
        setLoading(true);
        axios({
            method: "GET",
            url: "/api/keys",
            params: {
                page_size: pageSize,
                page_number: pageNumber,
                host: inputText.host,
                port: inputText.port,
                pattern: inputText.pattern,
            },
        })
        setLoading(false);
    }
    return (
        <div className="show-fake-browser ">
            <Container>
                <Header>
                    <Form layout="inline" model={model} onChange={onChange} formValue={inputText}>
                        <TextField name="host" label="Host"  style={{width: 300}}  onKeyUp={handlerKeys}/>
                        <TextField name="port" label="Port"  style={{width: 80}} onKeyUp={handlerKeys}/>
                        <TextField name="pattern" label="Pattern"  style={{width: 300}} onKeyUp={handlerKeys}/>
                        <Button type="submit" onClick={handleSubmit} appearance="primary">Show me!</Button>
                    </Form>
                </Header>
                <Content>

                    <div>
                        <Table height={420} data={data} loading={loading}>
                            <Column width={50} align="center" fixed>
                                <HeaderCell>Key</HeaderCell>
                                <Cell dataKey="key"/>
                            </Column>
                            <Column width={200} flexGrow={1}>
                                <HeaderCell>Value</HeaderCell>
                                <Cell dataKey="Value"/>
                            </Column>
                        </Table>
                        <div style={{padding: 20}}>
                            <Pagination
                                prev
                                next
                                first
                                last
                                ellipsis
                                boundaryLinks
                                maxButtons={5}
                                size="xs"
                                layout={['total', '-', 'limit', '|', 'pager', 'skip']}
                                total={total}
                                limit={pageSize}
                                limitOptions={[10, 20,50,100]}
                                activePage={pageNumber}
                                onChangePage={setPageNumber}
                                onChangeLimit={setPageSize}
                            />
                        </div>
                    </div>
                </Content>
            </Container>

        </div>)
}

export default MainContainer;