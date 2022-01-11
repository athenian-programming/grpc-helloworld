#!/usr/bin/env python
# -*- coding: utf-8 -*-

from __future__ import absolute_import
from __future__ import print_function

import logging
import sys

sys.path.append('../build/generated/source/python')

import helloworld_pb2
import grpc_info

logger = logging.getLogger(__name__)


def generate_hellos(name):
    for i in range(0, 10):
        yield helloworld_pb2.HelloRequest(name=name + "-" + str(i))


def main():
    name = "World"
    try:
        with grpc_info.GrpcInfo(host='localhost', port=50051) as grpcInfo:
            sayHello(grpcInfo, name)
            sayHelloWithManyRequests(grpcInfo, name)
            sayHelloWithManyReplies(grpcInfo, name)
            sayHelloWithManyRequestsAndReplies(grpcInfo, name)
    except BaseException as e:
        logger.error("Failure with: [{0}]".format(e))


def sayHello(grpcInfo, name):
    data = helloworld_pb2.HelloRequest(name=name)
    response = grpcInfo.stub.SayHello(data)
    print("sayHello() response: {}".format(response.message))


def sayHelloWithManyRequests(grpcInfo, name):
    generator = generate_hellos(name)
    response = grpcInfo.stub.SayHelloWithManyRequests(generator)
    print("\nsayHelloWithManyRequests() response: {}".format(response.message))


def sayHelloWithManyReplies(grpcInfo, name):
    print("\nsayHelloWithManyReplies() responses:")
    for response in grpcInfo.stub.SayHelloWithManyReplies(helloworld_pb2.HelloRequest(name=name)):
        print(response.message)


def sayHelloWithManyRequestsAndReplies(grpcInfo, name):
    generator = generate_hellos(name)
    print("\nsayHelloWithManyRequestsAndReplies() responses:")
    for response in grpcInfo.stub.SayHelloWithManyRequestsAndReplies(generator):
        print(response.message)


if __name__ == '__main__':
    logging.basicConfig()
    main()
