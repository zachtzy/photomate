# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at https://mozilla.org/MPL/2.0/.

"""
Scene Intelligence gRPC Server.
Provides real-time scene classification, composition analysis,
lighting assessment, and subject detection.
"""

import asyncio
import logging
import time
from concurrent import futures

import grpc
import numpy as np
from PIL import Image
import io

from scene_classifier.classifier import SceneClassifier
from composition_analyzer.analyzer import CompositionAnalyzer
from lighting_assessor.assessor import LightingAssessor

logger = logging.getLogger(__name__)

# Generated proto stubs would be imported here:
# from proto import scene_pb2, scene_pb2_grpc


class SceneIntelligenceServicer:
    """gRPC servicer for scene intelligence analysis."""

    def __init__(self):
        self.scene_classifier = SceneClassifier()
        self.composition_analyzer = CompositionAnalyzer()
        self.lighting_assessor = LightingAssessor()
        logger.info("Scene Intelligence service initialized")

    def AnalyzeFrame(self, request, context):
        """Analyze a single camera frame."""
        start_time = time.time()

        # Decode JPEG frame
        image = Image.open(io.BytesIO(request.frame_data))
        frame_np = np.array(image)

        # Run analyses in parallel using thread pool
        with futures.ThreadPoolExecutor(max_workers=3) as executor:
            scene_future = executor.submit(self.scene_classifier.classify, frame_np)
            composition_future = executor.submit(self.composition_analyzer.analyze, frame_np)
            lighting_future = executor.submit(self.lighting_assessor.assess, frame_np)

            scene_result = scene_future.result()
            composition_result = composition_future.result()
            lighting_result = lighting_future.result()

        processing_time_ms = int((time.time() - start_time) * 1000)

        # Build and return response
        # In production, this would construct the protobuf response
        return {
            "scene": scene_result,
            "composition": composition_result,
            "lighting": lighting_result,
            "processing_time_ms": processing_time_ms,
        }


def serve(port: int = 50051):
    """Start the gRPC server."""
    server = grpc.server(
        futures.ThreadPoolExecutor(max_workers=10),
        options=[
            ("grpc.max_receive_message_length", 10 * 1024 * 1024),  # 10MB
        ],
    )
    # scene_pb2_grpc.add_SceneIntelligenceServicer_to_server(
    #     SceneIntelligenceServicer(), server
    # )
    server.add_insecure_port(f"[::]:{port}")
    server.start()
    logger.info(f"Scene Intelligence gRPC server started on port {port}")
    server.wait_for_termination()


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)
    serve()
