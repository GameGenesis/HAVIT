import { Typography } from "@material-tailwind/react";

export default function Privacy() {
    return (
        <div className="home">
            <Typography
                as="big"
                variant="h1"
                className="text-5xl mr-4 cursor-pointer py-1.5 font-normal">
                    <b>Your Privacy.</b>
            </Typography>

            <br />

            <Typography
                as="big"
                variant="h1"
                className="text-3xl mr-4 cursor-pointer py-1.5 font-normal normal-case">
                    This page is used to inform the users of the website John Seong regarding our policies regarding the collection, use, and disclosure of Personal Information if anyone decided to use our service.{" "}
                    If you choose to use <span className="title">HAVIT.</span> then you agree to the collection and use of information in relation to this policy. The Personal Information that we collect are used for providing and improving the Service. We will not use or share your information with anyone except as described in this Privacy Policy.{" "}
                    The terms used in this Privacy Policy have the same meanings as in our Terms and Conditions, which are accessible on our official website unless otherwise defined in this Privacy Policy.
            </Typography>

            <br />

            <button className="mt-5 text-black bg-transparent border-2 border-black py-1 px-2 focus:outline-none hover:bg-black hover:text-white rounded text-lg">
                    <a href="https://johnseong.info/privacy" target="_blank" rel="noopener noreferrer">Visit <b>johnseong.info</b>.</a>
                </button>
        </div>
    );
}