import React, {SyntheticEvent, useState, KeyboardEvent, MouseEvent, useCallback} from "react";
import {Container, Header, Content, Form, Button, Pagination, Schema, Message, Panel} from "rsuite";

import {Table, Column, Cell, HeaderCell, ColumnGroup, TableProps} from 'rsuite-table';
import axios from "axios";

const TextField = (props: any) => {
    const {name, label, accepter, ...rest} = props;
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
    const [showError, setShowError] = useState(false);
    const [errorText, setErrorText] = useState(false);
    const [data, setData] = useState([]);
    const [total, setTotal] = useState(0);
    const [pageSize, setPageSize] = useState(10);
    const [pageNumber, setPageNumber] = useState(1);
    const handleSubmit = (checkStatus: boolean, e: SyntheticEvent<Element, Event>) => {
        if (checkStatus) {
            e.preventDefault();
            queryPage(pageNumber, pageSize);
        }
    };
    const handlerKeys = (e: KeyboardEvent<HTMLFormElement>) => {
        if (e.key === "Enter") {
            queryPage(pageNumber, pageSize);
            // queryData();
        }
    };

    const [inputText, setInputText] = useState({host: "", port: "", pattern: ""});

    const onChange = (e: Record<string, string>) => {
        setInputText({
            ...inputText,
            host: e.host,
            port: e.port,
            pattern: e.pattern,
        });
    }

    const resetError = (e: MouseEvent<Element>| undefined) => {
        setShowError(false);
    }

    const queryPage = useCallback(async (page, pageSize) => {
        setLoading(true);

        axios({
            method: "GET",
            url: "/api/keys",
            params: {
                page_size: pageSize,
                page_number: page,
                host: inputText.host,
                port: inputText.port,
                pattern: inputText.pattern,
            },
        }).then((response) => {
            setData(response.data.data);
            setTotal(response.data.total);
        }).catch((error) => {
            setErrorText(error.response.status)
            setShowError(true);
        }).finally(() => {
                setLoading(false);
            }
        )
    }, [setPageSize, setPageNumber, onChange])

    const onLimitChange = useCallback(async (newPageSize) => {
        try {
            await queryPage(pageNumber, newPageSize);
            setPageSize(newPageSize);
        } catch (error) {
            // nothing to do here, since we already alerted the user
        }
    }, [setPageSize, onChange])

    const onPageChange = useCallback(async (newPage) => {
        try {
            await queryPage(newPage, pageSize);
            setPageNumber(newPage);
        } catch (error) {
            // nothing to do here, since we already alerted the user
        }
    }, [setPageNumber, onChange])


    return (
        <div className="show-fake-browser ">
            <Container>
                <Header>

                    <Message showIcon type="error" header="Error"  hidden={!showError} onClose={resetError}>
                        An unexpected error occurred on the server: {errorText}
                    </Message>

                    <Panel header="Enter connection information for your NiFi Distributed MapCache Server">
                        <Form layout="inline" model={model} onChange={onChange} onSubmit={handleSubmit}
                              formValue={inputText}>
                            <TextField name="host" label="Host" style={{width: 300}} onKeyUp={handlerKeys}/>
                            <TextField name="port" label="Port" style={{width: 80}} onKeyUp={handlerKeys}/>
                            <TextField name="pattern" label="Pattern" style={{width: 300}} onKeyUp={handlerKeys}/>
                            <Button type="submit" appearance="primary">Show me!</Button>
                        </Form>
                    </Panel>
                </Header>
                <Content>

                    <Table height={420} data={data} loading={loading} hover={true} autoHeight={true} bordered={true}
                           cellBordered={true}>
                        <Column align="center" resizable minWidth={350} width={350}>
                            <HeaderCell>Key</HeaderCell>
                            <Cell dataKey="key"/>
                        </Column>
                        <Column resizable flexGrow={1}>
                            <HeaderCell>Value</HeaderCell>
                            <Cell dataKey="value"/>
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
                            limitOptions={[10, 20, 50, 100, 1000]}
                            activePage={pageNumber}
                            onChangePage={onPageChange}
                            onChangeLimit={onLimitChange}
                        />
                    </div>
                </Content>
            </Container>
        </div>)
}

export default MainContainer;